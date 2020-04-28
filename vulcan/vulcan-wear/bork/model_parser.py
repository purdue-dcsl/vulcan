import json
import logging
import sys
import os
import networkx as nx
import matplotlib.pyplot as plt

# constants
OUTPUT_FILE = 'model.js'

class ModelParser(object):
    """
    Bork. The main class in charge to parse the stateful model.
    """

    def __init__(self,
                 input_graph=None,
                 output_dir=None):
        self.logger = logging.getLogger(self.__class__.__name__)

        # utg
        self.G = nx.DiGraph()
        # state model
        self.G_model = nx.DiGraph()

        self.app = {}
        self.contracted_edges = []
        self.nodes = {}

        self.output_dir = output_dir

        self.load_graph(input_graph)

    def load_graph(self, utg):
        """
        Import json to a NetworkX DiGraph
        :param input_graph:
        :return:
        """
        utg_json = json.load(open(utg))
        nodes = utg_json['nodes']
        edges = utg_json['edges']


        # extract basic info from json
        self.app['sha256'] = utg_json['app_sha256']
        self.app['package'] = utg_json['app_package']
        self.app['main_activity'] = utg_json['app_main_activity']
        self.app['num_total_activites'] = utg_json['app_num_total_activities']
        self.first = utg_json['first_activity']

        for node in nodes:
            self.G.add_node(node['id'], label=node['label'])

        state_int = 0
        for edge in edges:

            if edge['wear_events']:
                self.G.add_node(state_int, label='FSM', attrib=edge['wear_events'])
                self.G.add_edge(edge['from'], state_int)
                self.G.add_edge(state_int, edge['to'])
                state_int += 1
            else:
                cntr_edge = {
                    'from': edge['from'],
                    'to': edge['to']
                }
                self.contracted_edges.append(cntr_edge)
                self.G.add_edge(edge['from'], edge['to'], wear_events=edge['wear_events'])

        print(self.G.node[self.first])

    def run(self):
        """
        run the parser
        :return:
        """
        print('running ...')
        # contract edges, to remove redundancy and edges/nodes not relevant to the wearable model
        G_minor = self.G.copy()
        for edge in self.contracted_edges:
            print('-> ', edge['from'], edge['to'])
            # try:
            G_minor = self.__contracted_edges(G_minor, edge['from'], edge['to'])
            # except Exception as ex:
            #     print('**error ', ex)

        self.G_model = G_minor

        # show contracted graph
        # print(list(nx.dfs_edges(G_minor)))
        # self.__vis(G_minor)

        # output
        self.__output_model()

    def __vis(self, G):
        """
        Visualize FSM

        tkinter module (for Python 3.x.x) is needed:
        sudo apt-get install python3-tk
        """
        nx.draw_networkx(G)
        plt.show()

    def __get_node(self, node):
        if self.nodes.get(node) is None:
            self.nodes[node] = node
        return self.nodes[node]

    def __set_node(self, node, value):
        self.nodes[node] = value

        # updates nodes that are still have the reference for {node}
        for key, val in self.nodes.items():
            if val == node:
                self.nodes[key] = value

    def __contracted_edges(self, G, node_from, node_to):
        '''
        contract edges (u,v) in a Graph G
        :param G: NetworkX Graph
        :param node_from: node u of an edge (u,v)
        :param node_to: node v of an edge (u,v)

        :return: A network graph where node v will be merge into u
        '''
        u = self.__get_node(node_from)
        v = self.__get_node(node_to)
        M = nx.contracted_edge(G, (u,v))

        # since the node v is merged with u, we save the reference in case that there an edges that needs to be
        # contracted which includes node v.
        self.__set_node(v, u)
        return M

    def __output_model(self):
        """
        Output the current model to a js file
        """

        model_file_path = os.path.join(self.output_dir, OUTPUT_FILE)
        model_file = open(model_file_path, "w")
        model_states = []
        for state_str in self.G_model.nodes():
            state = self.G_model.nodes[state_str]

            node = {
                "id": state_str,
                "info": state
            }

            model_states.append(node)

        model = {
            "app_sha256": self.app['sha256'],
            "app_package": self.app['package'],
            "app_main_activity": self.app['main_activity'],
            "app_num_total_activities": self.app['num_total_activites'],

            "num_states": len(model_states),

            "states": model_states

        }

        model_json = json.dumps(model, indent=2)
        model_file.write(model_json)
        model_file.close()
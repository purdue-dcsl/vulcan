# helper file of bork
# start parse module (to build up the stateful model for the wearable app)
import argparse

from bork.model_parser import ModelParser


INPUT_FILE = '/home/ebarsallo/Code/purdue.edu/699-research/android-wear/droidbot-wear/target/tmp/runkeeper.pro-181022-12/utg.json'
OUTPUT_DIR = '/home/ebarsallo/Code/purdue.edu/699-research/android-wear/droidbot-wear/target/tmp/runkeeper.pro-181022-12/'

def parse_args():
    """
    parse command line input
    generate options
    :return:
    """
    parser = argparse.ArgumentParser(description="Start Bork Model Parser",
                                     formatter_class=argparse.RawTextHelpFormatter)

    # TODO: temporarily set on FALSE for tests
    parser.add_argument("-i", action="store", dest="input_graph", required=False,
                        help="The graph to be used as input (usually named utg.js)")
    parser.add_argument("-o", action="store", dest="output_dir",
                        help="The directory of output")

    options = parser.parse_args()
    return options



def main():
    """
        the main function
        it starts the replay module according to the arguments given in cmd line

        Usage:
        python -m bork.start_parser

    """
    opts = parse_args()

    parser = ModelParser(
        # input_graph=opts.input_graph
        input_graph=INPUT_FILE,
        output_dir=OUTPUT_DIR
    )

    parser.run()

if __name__ == "__main__":
    main()

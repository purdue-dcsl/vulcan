#!/bin/python

import xlrd
import json 

# Params
INPUT_FILE = 'IntentSpecificationMapping.xlsx'
SHEET_NAME_ACTION_EXTRA = 'Action | Extra'
SHEET_NAME_ACTION_DATA = 'Action | Data'
SHEET_NAME_ACTION = 'Action'
SHEET_NAME_EXTRA = 'Extra'

JSON_FIELDNAME_ACTION = 'action'
JSON_FIELDNAME_DATA = 'data'
JSON_FIELDNAME_EXTRA = 'extra_field'
JSON_FIELDNAME_ACTION_EXTRA = 'action_extra'

totals = {}
gen = {}

def parse_spreadsheet():
    """
    Parse input and convert the content in a json file
    """

    book = xlrd.open_workbook(INPUT_FILE)
    for sheet in book.sheets():
        #print("sheet: '{}'".format(sheet.name), "has {} rows".format(sheet.nrows))
        totals[sheet.name] = sheet.nrows - 1  # header row

        if sheet.name == SHEET_NAME_ACTION_EXTRA:

            #  0: ACTION
            #  1: API_LEVEL	
            #  2: ACTION_CONSTANT	
            #  3: EXTRA_FIELD	
            #  4: EXTRA_FIELD_OPTIONAL
            
            action_extra_map = {}
            for r_id in range(1, sheet.nrows):
                row = sheet.row_values(r_id)
                action_extra_map[r_id]={ 
                        "action_name": row[0], 
                        "extra_name":row[3], 
                        "optional": bool(row[4])
                    }
        
        elif sheet.name == SHEET_NAME_ACTION_DATA:

            #  0: ACTION	
            #  1: API_LEVEL	
            #  2: ACTION_CONSTANT	
            #  3: DATA_TYPE
            #  4: DATA_TYPE_AGGR 	
            #  5: DATA_OBS	
            #  6: DATA_OPTIONAL
            
            action_data_map = {}
            for r_id in range(1, sheet.nrows):
                row = sheet.row_values(r_id)
                action_data_map[row[0]] = {
                        "type": row[3], 
                        "obs": row[4], 
                        "optional": bool(row[6])
                    }

        elif sheet.name == SHEET_NAME_ACTION:
            
            #  0: ACTION	
            #  1: API_LEVEL	
            #  2: API_LEVEL_DEPRECATED	
            #  3: ACTION_CONSTANT	
            #  4: MIME_TYPE	
            #  5: CATEGORY	
            #  6: ACTION_ACTIVITY	
            #  7: ACTION_BROADCAST	
            #  8: ACTION_OBS	
            #  9: PROTECTED	
            # 10: PERMISSION	
            # 11: PERMISSION_LEVEL 

            action = {}
            for r_id in range(1, sheet.nrows):
                row = sheet.row_values(r_id)
                perm = []
                if row[10] != "":
                    perm = [row[10]]

                # This is the full action field in json
                action[row[0]] = {
                        "api_level": int(row[1]),
                        "constant_value": row[3], 
                        "activity":bool(row[6]), 
                        "broadcast":bool(row[7]),
                        "category": row[5],
                        "mime_type": row[4],
                        "wearable_specific": False, 
                        "obs": row[8], 
                        "protected": bool(row[9]), 
                        "permission": perm, 
                        "permission_level":row[11]
                    }

                # Only add api_level or api_level_deprecated when the value is an integer
                if row[2] != "":
                    action[row[0]]["api_level_deprecated"] =  int(row[2])
                    
                
        elif sheet.name == SHEET_NAME_EXTRA:

            #  0: EXTRA_FIELD	
            #  1: EXTRA_FIELD_CONSTANT
            #  2: API_LEVEL 
            #  3: API_LEVEL_DEPRECATED	
            #  4: EXTRA_FIELD_TYPE	
            #  5: EXTRA_FIELD_TYPE_AGGR
            #  6: EXTRA_FIELD_OBS

            extra = {}
            for r_id in range(1, sheet.nrows):
                row = sheet.row_values(r_id)
                extra[row[0]] = {
                        "constant_value": row[1],
                        "type":row[4], 
                        "type_aggr":row[5], 
                        "obs": row[6]
                    }

                # Only add api_level or api_level_deprecated when the value is an integer
                if row[2] != "":
                    extra[row[0]]["api_level"] =  int(row[2])

                if row[3] != "":
                    extra[row[0]]["api_level_deprecated"] =  int(row[3])
                     
        else:
            pass

    # for act, fields in action_extra_map.items():
    #     extra_in_action = fields["extra_field"]
    #     if extra_in_action in extra:
    #         extra[extra_in_action]["action"] = act
    #         extra[extra_in_action]["optional"] = fields["extra_optional"]

    json_dict = {}
    json_dict[JSON_FIELDNAME_ACTION] = []
    json_dict[JSON_FIELDNAME_DATA] = []
    json_dict[JSON_FIELDNAME_EXTRA] = []
    json_dict[JSON_FIELDNAME_ACTION_EXTRA] = []

    for act, fields in action_data_map.items():
        fields["action_name"] = act
        json_dict[JSON_FIELDNAME_DATA].append(fields)

    for act, fields in action.items():
        fields["action_name"] = act
        json_dict[JSON_FIELDNAME_ACTION].append(fields)

    for ext, fields in extra.items():
        fields["extra_name"] = ext
        json_dict[JSON_FIELDNAME_EXTRA].append(fields)

    for _, fields in action_extra_map.items():
        json_dict[JSON_FIELDNAME_ACTION_EXTRA].append(fields)

    with open("specs.json", 'w') as o:
        json.dump(json_dict, o, sort_keys=True, indent=4)

    gen[JSON_FIELDNAME_ACTION] = len (json_dict[JSON_FIELDNAME_ACTION])
    gen[JSON_FIELDNAME_DATA] = len (json_dict[JSON_FIELDNAME_DATA])
    gen[JSON_FIELDNAME_EXTRA] = len (json_dict[JSON_FIELDNAME_EXTRA])
    gen[JSON_FIELDNAME_ACTION_EXTRA] = len (json_dict[JSON_FIELDNAME_ACTION_EXTRA])

def report():
    print(' \t== Report ==')
    print(' Actions: %d out of %d' % (gen[JSON_FIELDNAME_ACTION], totals[SHEET_NAME_ACTION]))
    print(' Extras: %d out of %d' % (gen[JSON_FIELDNAME_EXTRA], totals[SHEET_NAME_EXTRA]))
    print(' Actions w/Data: %d out of %d' % (gen[JSON_FIELDNAME_DATA], totals[SHEET_NAME_ACTION_DATA]))
    print(' Actions w/Extra Field: %d out of %d' % (gen[JSON_FIELDNAME_ACTION_EXTRA], totals[SHEET_NAME_ACTION_EXTRA]))


def main():

    parse_spreadsheet()
    report()

if __name__ == "__main__":
    main()
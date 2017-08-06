import datetime

import logging

import model
import utils

class OpenXCStats(object):
    def __init__(self):
        pass

    def add(self, **data):
        entry = self.get_datastore_entity(data)
        entry.put()
        return self.get_json_object(entry)

    def get(self, debug=False, **filters):
        query_string = "select * from OpenXCStats"

        filters = {key: val for key, val in filters.iteritems() if val != None}

        i = 0
        for field in filters:
            if i == 0:
                query_string += " where "

            if i < len(filters) - 1:
                query_string += "%s='%s' and " % (field, filters[field])
            else:
                query_string += "%s='%s'" % (field, filters[field])
            i += 1

        response = utils.fetch_gql(query_string)
        if debug:
            logging.error("Query String: %s\n\n Response Length: %s" % (query_string, len(response)))

        return response


    def fetch_all(self):
        all_entries = self.get()

        response = []
        for entry in all_entries:
            response.append(self.get_json_object(entry))

        return response

    @staticmethod
    def get_json_object(datastore_entity):
        json_object = {
            "user_id": datastore_entity.user_id,
            "car_id": datastore_entity.car_id,
            "measurement_type": datastore_entity.measurement_type,
            "measurement_value": datastore_entity.measurement_value,
            "measurement_unit": datastore_entity.measurement_unit,
            "created_at": int(datastore_entity.created_at.strftime("%s")),
        }

        return json_object

    @staticmethod
    def get_datastore_entity(json_object):
        entry = model.OpenXCStats()
        entry.user_id = json_object.get("user_id", None)
        entry.car_id = json_object.get("car_id", None)
        entry.measurement_type = json_object.get("measurement_type", None)
        entry.measurement_value = json_object.get("measurement_value", None)
        entry.measurement_unit = json_object.get("measurement_unit", None)
        entry.created_at = datetime.datetime.fromtimestamp(json_object.get("created_at", int(datetime.datetime.now().strftime('%s'))))

        return entry
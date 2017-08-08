import json
import traceback

import time
import webapp2
from google.appengine.api import taskqueue
from jose import jws

from .models import *

from google.appengine.ext import db


class OpenXCStatsHandler(webapp2.RequestHandler):
    queue_name = "openxc-stats-pull"

    def fetch_user_data(self, user_id):
        user_email = utils.authenticate_user(self, self.request.url, ["jigarkub@usc.edu", "youngcho@isi.edu"])
        if not user_email:
            return

        self.response.headers['Content-Type'] = "application/json"
        self.response.headers['Access-Control-Allow-Origin'] = '*'

        try:
            entries = utils.fetch_gql("select * from OpenXCStats where user_id='{}' order by created_at desc limit 50".format(user_id))
            response = []
            for entry in entries:
                response.append(OpenXCStats.get_json_object(entry))

            self.response.out.write(json.dumps({'success': True, 'error': [], 'response': response}))
        except Exception as e:
            self.response.out.write(json.dumps({'success': False, 'error': e.message, 'response': None}))
            logging.error(traceback.format_exc())

    def add_to_pull(self):
        self.response.headers['Content-Type'] = "application/json"
        self.response.headers['Access-Control-Allow-Origin'] = '*'

        try:
            authorization = self.request.headers.get('Authorization', '').split()
            auth_type = authorization[0]
            auth_token = authorization[1]
            user_info = json.loads(jws.verify(auth_token, 'insecure secret', algorithms=['HS256']))
            user_id = user_info.get('user_id')
            full_name = user_info.get('full_name')

            payload = json.loads(self.request.body)
            for entity in payload:
                modified_entity = {
                    'user_id': user_id,
                    'measurement_type': entity.get('name'),
                    'measurement_value': json.dumps(entity.get('value')),
                    'measurement_key': json.dumps(entity.get('mKey')),
                    'created_at': entity.get('timestamp')
                }

                utils.insert_in_pull_queue(
                    queue_name="openxc-stats-pull",
                    payload=json.dumps(modified_entity)
                )
            self.error(201)
        except:
            self.error(500)
            logging.error(traceback.format_exc())

    def consume_pull(self):
        if "X-AppEngine-Cron" in self.request.headers:
            try:
                q = taskqueue.Queue(self.queue_name)
                stats = q.fetch_statistics()
                task_in_queue = stats.tasks
                logging.error(str(task_in_queue))
                count = task_in_queue / 20000.0
                while count > 0:
                    url = '/openxc_stats/consume_pull'
                    taskqueue.add(queue_name='general-stats', url=url)
                    count -= 1
            except:
                logging.error(traceback.format_exc())
            return

        lease_seconds = 10
        max_tasks = 1000
        num_merge = 100
        start_time = time.time()
        q = taskqueue.Queue(self.queue_name)
        while True:
            if time.time() - start_time > 50:
                break
            try:
                tasks = q.lease_tasks(lease_seconds, max_tasks)
                if len(tasks) == 0:
                    return
            except:
                logging.error(traceback.format_exc())
                return

            task_sub_lists = [tasks[i:i + int(num_merge)] for i in range(0, len(tasks), int(num_merge))]
            for sub_list in task_sub_lists:
                entities = []
                for task in sub_list:
                    entities.append(OpenXCStats.get_datastore_entity(json.loads(task.payload)))

                try:
                    db.put(entities)
                    q.delete_tasks(sub_list)
                except:
                    logging.error(traceback.format_exc())

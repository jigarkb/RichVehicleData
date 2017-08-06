import json
import traceback

import time
import webapp2
from google.appengine.api import taskqueue

from .models import *

from google.appengine.ext import db

class OpenXCStatsHandler(webapp2.RequestHandler):
    queue_name = "openxc-stats-pull"
    def add_to_pull(self):
        self.response.headers['Content-Type'] = "application/json"
        self.response.headers['Access-Control-Allow-Origin'] = '*'

        try:
            payload = json.loads(self.request.get("payload", None))

            for entity in payload:
                entity.update({'created_at': int(datetime.datetime.now().strftime('%s'))})
                utils.insert_in_pull_queue(
                    queue_name="openxc-stats-pull",
                    payload=json.dumps(entity)
                )

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
                count = task_in_queue/20000.0
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


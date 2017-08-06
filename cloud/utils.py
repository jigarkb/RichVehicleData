from google.appengine.api import taskqueue

def insert_in_pull_queue(queue_name, payload):
    q = taskqueue.Queue(queue_name)
    q.add((taskqueue.Task(payload=payload, method='PULL')))

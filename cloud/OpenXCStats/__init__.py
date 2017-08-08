from .handlers import *

app = webapp2.WSGIApplication([
    webapp2.Route(template='/openxc_stats/add_to_pull',
                  handler=OpenXCStatsHandler,
                  handler_method='add_to_pull',
                  methods=['GET', 'POST']),

    webapp2.Route(template='/openxc_stats/consume_pull',
                  handler=OpenXCStatsHandler,
                  handler_method='consume_pull',
                  methods=['GET', 'POST']),

    webapp2.Route(template='/openxc_stats/fetch_user_data/<:([^/]+)?>',
                  handler=OpenXCStatsHandler,
                  handler_method='fetch_user_data',
                  methods=['GET', 'POST']),
])
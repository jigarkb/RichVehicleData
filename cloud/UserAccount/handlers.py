import json
import traceback

import time
import webapp2
from google.appengine.api import taskqueue

from .models import *
from passlib.hash import pbkdf2_sha256
from google.appengine.ext import db

class UserAccountHandler(webapp2.RequestHandler):
    def login(self):
        self.response.headers['Content-Type'] = "application/json"
        self.response.headers['Access-Control-Allow-Origin'] = '*'

        try:
            user_id = self.request.get("user_id", None)
            password = self.request.get("password", None)

            user_account = UserAccount()
            response = user_account.login(
                user_id=user_id,
                password=password,
            )

            if not response:
                raise Exception('Invalid Credentials!')

            self.response.out.write(json.dumps({'success': True, 'error': [], 'response': response}))
        except Exception as e:
            self.response.out.write(json.dumps({'success': False, 'error': e.message, 'response': None}))
            logging.error(traceback.format_exc())

    def register(self):
        self.response.headers['Content-Type'] = "application/json"
        self.response.headers['Access-Control-Allow-Origin'] = '*'

        try:
            user_id = self.request.get("user_id", None)
            password = self.request.get("password", None)
            full_name = self.request.get("full_name", None)


            user_account = UserAccount()
            response = user_account.register(
                user_id=user_id,
                password_hash=pbkdf2_sha256.hash(password),
                full_name=full_name,
            )

            self.response.out.write(json.dumps({'success': True, 'error': [], 'response': response}))
        except Exception as e:
            self.response.out.write(json.dumps({'success': False, 'error': e.message, 'response': None}))
            logging.error(traceback.format_exc())

    def verify(self):
        self.response.headers['Content-Type'] = "application/json"
        self.response.headers['Access-Control-Allow-Origin'] = '*'

        try:
            auth_token = self.request.get("auth_token", None)

            user_account = UserAccount()
            response = user_account.verify(
                auth_token=auth_token,
            )

            self.response.out.write(json.dumps({'success': True, 'error': [], 'response': response}))
        except Exception as e:
            self.response.out.write(json.dumps({'success': False, 'error': e.message, 'response': None}))
            logging.error(traceback.format_exc())
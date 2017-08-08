from google.appengine.ext import db


class OpenXCStats(db.Model):
    user_id = db.StringProperty()
    car_id = db.StringProperty()
    measurement_type = db.StringProperty()
    measurement_value = db.StringProperty()
    measurement_key = db.TextProperty()
    created_at = db.DateTimeProperty()


class UserAccount(db.Model):
    user_id = db.StringProperty()
    password_hash = db.TextProperty()
    full_name = db.StringProperty()

    created_at = db.DateTimeProperty(auto_now_add=True)
    modified_at = db.DateTimeProperty(auto_now=True)

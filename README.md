#Introduction
This project aims to collect On Board Diagnostic data and store it in cloud database. It makes use of OpenXC Android Library.

#Documentation
1. Register in the app

![Alt text](screenshots/register.png?raw=true "Register")

2. Login in the app

![Alt text](screenshots/login.png?raw=true "Login")

3. Welcome: Start Capturing

![Alt text](screenshots/StartCapture.png?raw=true "Register")

4. Capturing data to cloud

![Alt text](screenshots/StopCapture.png?raw=true "Register")

5. Query to get JSON response for last few entries of the user
```https://brainstorm-cloud.appspot.com/openxc_stats/fetch_user_data/<user_id>```

![Alt text](screenshots/APIFetchUserData.png?raw=true "Register")

6. Sensor data stored in Google Cloud Datastore

![Alt text](screenshots/CapturedData.png?raw=true "Register")

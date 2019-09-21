# TranSleep

Welcome to TranSleep!<br>
Helping commuters turn their tran**sit** experience into a relaxing tran**sleep** experience!

![Transleep](https://user-images.githubusercontent.com/22537909/65367881-8fae7380-dc06-11e9-96f8-53148f17dbb7.jpg)

This app was developed in 2017 at Hack The Valley to help tackle the issue of commuters missing their transit stops because they fell asleep. The application is programmed to wake users up from their commute based on how close they are to their stop. The user is required to specify the details of their stop and the vehicle they are taking. Based on the provided information, we leveraged the built in GPS on the user's phone along with their input data (bus number and desired stop) to alert them when they are close to their stop. Specifically, we triggered the phone's alarm and vibration when users were within 1km of their destination so that users could be notified (woken up from their sleep) on their commute to get off the correct stop.

The frontend of the application was built using Android Studios, and the backend was built using Flask-RESTful by using open public transit data from various transit agencies to build a database.

# shake
A system for sharing contact information at the shake of a hand. Developed as independent research at Williams College, investigating HCI, machine learning, and distributed systems.

## Overview
Sharing contact information in a face-to-face exchange has not changed since the rise of cell phone contact management applications. We have failed to harness the human behavior which signals that such an information exchange should take place. That is, the handshakes, fist-bumps, and other gestures which are native to two individuals becoming connected.

This application leverages a pebble smartwatch ( now fitbit ) to collect accelerometer data and detect handshakes. The system is composed of three parts - a native pebble application, an android application, and a server-side php script for matching two handshakes. We store recognized gesture vectors sent by individual applications in a MySQL database, partitioned by geolocation for increased query performance. A matchmaking php script finds vectors in the database which match, and sends contact information result back to an individual's phone in JSON format.

## Pebble Application
The first version was composed of a pebble watch app in standard C, and used Pebble Java SDK to send messages between the watch and the phone application.

The second version bypasses the phone application completely, leveraging Pebble javascript API to communicate with the matchmaking server directly.

## Android Application
Included in the first version only. A minimal, wireframe application used to receive accelerometer data from the pebble smartwatch, and send gesture recognition instances to the matchmaking server.

### Recognized Gesture Instance
This is the primary piece of data used for matching two individuals.

```
<TIME, <X_ACCEL_VEC>, <Y_ACCEL_VEC>, <Z_ACCEL_VEC>>
```

## Server
A matchmaking script written in PHP receives a client request for a match, and finds a recognized gesture in the database with the closest distance - according to a distance function with many possible implementations. If that distance is below the configured threshold, then contact information is returned in JSON format. 

# RocketSingh
Bike hailing tech to create an infrastructure for Uber for Bikes usecase. This project is an attempt to replicate the taxi aggregation service using bikes as the transport medium.
They will be two user facing apps: 
- One for the customer(to be pillion) 
- Other for the Biker who has under this tech platform to serve the customers. 

Both the apps have all the entire user interaction flow implemented like : 
- Register and Login
- Search for Bikers nearby
- Request for a biker at a location
- Send promocodes; Avail promocodes

Both the apps have been coded to be friendly on the resource intensive mobile environment. They use event driven Publish-Subscribe mechanism avoiding polling and Byte compression protocols to avoid mobile data drain.

Many UI patterns like Navigation pattern and route drawing on map have been coded form ground principles instead of using the the readymade libraries because these functionalities were implemented before these libraries were stable.

Note: Many design patterns used here were inspired from the Wordpress mobile opensoure code.

This project is a server application written in Java. The purpose of this server was to demonstrate the capabilities of network sockets.
Specifically, this server is multithreaded capable of identifying remote admin and normal client users. The remote clients are designed to be
accessed via Android applications. The remote admins access the server through a dedicated Java desktop application. This server is mainly a
proof of concept and was done for East Stroudsburg Univerity's CPSC Networking class. It is not inteded to be a production server. There are
security issues. Please use this as a learning experience. The code is well commented.

The goal of this server application is to register users as students and provide them with a resource for remote test administration. A student user 
can access the server, select a test to take, and then have the test graded by the server. Upon completion of the test, the server will send back the results 
to the student and include there overall averages for taking the test. The admins can log in and add, delete, modify, and view tests and test results 
through a simple wizard style interface. 

This project was built with NetBeans IDE and should be able to load and build without any modifications. The target system for deployment is Linux but
all development was done on Mac OS X. Windows should work without modification but this has not been tested.

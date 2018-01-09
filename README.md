# Console chat server.
- When a new user is connected, the server sends him the last N messages from other users connected to the server.
- When receiving messages from users, the server also records the accompanying information (date and time of the message, user name, IP address and current mode).
- Server settings (port, N last messages, maximum number of connected users) are installed in the config file (XML).
- Users when connected to the server receive a generated name, which can be changed and registered on the server. If the name is registered, you must enter the password when changing the name. Data about users is also stored in a file (XML).

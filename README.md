# Java-TwitchBot
It's a bot for twitch written in Java

It's now refactored. You can add new bot accounts in the accounts.cfg
They will be automatically loaded when the bot starts. 

Modules are here also. To create a module, just make an object that extends Module, and then serialize it and put it in Modules or share. Serialized modules put in the modules folder will be loaded on startup.

There's also a GUI now! It's pretty basic for the moment, but it allows you to interact with and view the chat for every channel the bot is in. 
![alt tag](http://i.imgur.com/mjSgxFq.png)

Feel free to try out the bot in any way you'd like. The more testers, the more bugs I can fix.

Ignore most of the documentation for the time being. I haven't had the time to update it since the refactor.

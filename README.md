# Java-TwitchBot
It's a bot for twitch written in Java

It's now refactored. You can add new bot accounts in the accounts.cfg
They will be automatically loaded when the bot starts. 

Modules are here also. To create a module, just make an object that extends Module, and then serialize it and put it in Modules or share. Serialized modules put in the modules folder will be loaded on startup.

There's also a GUI now! It's pretty basic for the moment, but it allows you to interact with and view the chat for every channel the bot is in. 
![alt tag](http://i.imgur.com/mjSgxFq.png)

Feel free to try out the bot in any way you'd like. The more testers, the more bugs I can fix. One known bug (that isn't really my fault, nor can I do anything about it) is that the JSON object for chat users will sometimes "demote" players to viewer, even if they're mods. This is an artefact of the JSON, not the program. Also, sometimes the Group IRC connection just fails to read anything, for whatever reason, and will cause broken pipe exceptions. If this happens, just restart the bot until the group connection starts up properly.

Ignore most of the documentation for the time being. I haven't had the time to update it since the refactor.

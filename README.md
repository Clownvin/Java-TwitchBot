# Java-TwitchBot
It's a bot for twitch written in Java

It's now refactored. You can add new bot accounts in the accounts.cfg
They will be automatically loaded when the bot starts. 

Modules are here! To create a module, simply create a class that extends Module, and add it to the moduleList near the begining of the static block in ModuleManager. When the bot is compiled and run, it will automatically create the module ".ser" file in the modules folder. Implementing modules is a bit more difficult than just adding the module to the modules folder. You'll need to add the bytecode for the Module as well.

If you're using a JAR version, you just need to open up the JAR in an Archive Manager and navigate to "com.github.clownvin.jtwitchbot.modules", and add the bytecode files for the module. For non-jar users, either add the Module source, and perform the same steps as the paragraph above, or add the bytecode files to the same location as in JAR, but in the "bin" folder instead.

There's also a GUI now! It's pretty basic for the moment, but it allows you to interact with and view the chat for every channel the bot is in. 
![alt tag](http://i.imgur.com/mjSgxFq.png)

Feel free to try out the bot in any way you'd like. The more testers, the more bugs I can fix. One known bug (that isn't really my fault, nor can I do anything about it) is that the JSON object for chat users will sometimes "demote" players to viewer, even if they're mods. This is an artefact of the JSON, not the program. Also, sometimes the Group IRC connection just fails to read anything, for whatever reason, and will cause broken pipe exceptions. If this happens, just restart the bot until the group connection starts up properly.

Ignore most of the documentation for the time being. I haven't had the time to update it since the refactor.

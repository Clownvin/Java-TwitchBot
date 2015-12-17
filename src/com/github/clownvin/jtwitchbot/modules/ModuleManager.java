package com.github.clownvin.jtwitchbot.modules;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.List;

import com.github.clownvin.jtwitchbot.messaging.Message;
import com.github.clownvin.jtwitchbot.user.User;

public final class ModuleManager {
	public static final String MODULE_DIRECTORY = "./modules/";
	private static final List<Module> moduleList = new ArrayList<Module>();
	
	//Static block loads modules from the module directory.
	static {
		File moduleFolder = new File(MODULE_DIRECTORY);
		if (!moduleFolder.exists() || !moduleFolder.isDirectory()) {
			System.out.println("Cannot load modules from a directory that doesn't exist, or is a regular file.");
			System.out.println("Please create a folder in the root directory of this program names \"modules\", and put any modules in it.");
		} else {
			for (File module : moduleFolder.listFiles()) {
				if (!module.getName().endsWith(".ser")) {
					continue;
				}
				ObjectInputStream in = null;
				try {
					in = new ObjectInputStream(new FileInputStream(module));
					moduleList.add((Module)in.readObject());
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				} finally {
					if (in == null) {
						continue;
					}
					try {
						in.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
			for (Module module : moduleList) {
				module.onLoad();
			}
		}
	}
	
	public static void onMessage(Message message) {
		for (Module module : moduleList) {
			if (module.onMessage(message)) {
				return;
			}
		}
	}
	
	public static void onWhisper(Message message) {
		for (Module module : moduleList) {
			if (module.onWhisper(message)) {
				return;
			}
		}
	}
	
	public static void onLeave(User user) {
		for (Module module : moduleList) {
			if (module.onLeave(user)) {
				return;
			}
		}
	}
	
	public static void onJoin(User user) {
		for (Module module : moduleList) {
			if (module.onJoin(user)) {
				return;
			}
		}
	}
	
	public static void onCommand(User user, String command, String[] args) {
		for (Module module : moduleList) {
			if (module.onCommand(user, command, args)) {
				return;
			}
		}
	}
}

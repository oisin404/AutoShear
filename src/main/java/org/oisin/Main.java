package org.oisin;

import org.rusherhack.client.api.RusherHackAPI;
import org.rusherhack.client.api.plugin.Plugin;

/**
 * AutoShear
 *
 * @author oisin404
 */
public class Main extends Plugin {
	
	@Override
	public void onLoad() {
		
		//logger
		this.getLogger().info("AutoShear locked n' loaded!");
		
		//creating and registering a new module
		final AutoShear autoShear = new AutoShear();
		RusherHackAPI.getModuleManager().registerFeature(autoShear);
	}
	
	@Override
	public void onUnload() {
		this.getLogger().info("AutoShear unloaded!");
	}
	
}
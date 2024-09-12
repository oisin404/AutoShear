package org.oisin;

import org.rusherhack.client.api.RusherHackAPI;
import org.rusherhack.client.api.plugin.Plugin;

/**
 * AutoPumpkin
 *
 * @author oisin404
 */
public class Main extends Plugin {
	
	@Override
	public void onLoad() {
		
		//logger
		this.getLogger().info("AutoPumpkin loaded!");
		
		//creating and registering a new module
		final AutoPumpkin autoPumpkin = new AutoPumpkin();
		RusherHackAPI.getModuleManager().registerFeature(autoPumpkin);
	}
	
	@Override
	public void onUnload() {
		this.getLogger().info("AutoPumpkin unloaded!");
	}
	
}
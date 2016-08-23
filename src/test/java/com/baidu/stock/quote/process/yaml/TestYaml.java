package com.baidu.stock.quote.process.yaml;

import java.io.File;
import java.io.FileNotFoundException;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import com.baidu.stock.process.config.setting.MarketTime;



public class TestYaml {
	
	public static void main(String[] args) {
		DumperOptions options = new DumperOptions();
		options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
		
		Yaml yaml = new Yaml(options);
		MarketTime marketTime = new MarketTime();
		marketTime.setAuctionTime("9:15");
		System.out.println(yaml.dump(marketTime));
	}
}
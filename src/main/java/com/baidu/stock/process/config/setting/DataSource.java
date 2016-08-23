package com.baidu.stock.process.config.setting;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 
 * @author dengjianli
 *
 */
    @Component
	@ConfigurationProperties(prefix="hqRunTimeConfig.dataSource")
	public class DataSource extends BaseDataSource{
		private List<BaseDataSource>lst;

		public List<BaseDataSource> getLst() {
			return lst;
		}

		public void setLst(List<BaseDataSource> lst) {
			this.lst = lst;
		}
	}
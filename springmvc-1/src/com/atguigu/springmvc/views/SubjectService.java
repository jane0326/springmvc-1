package com.atguigu.springmvc.views;
import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.CrossOrigin;

import com.bonc.dw3.common.datasource.DynamicDataSourceContextHolder;
import com.bonc.dw3.common.util.DateUtils;
import com.bonc.dw3.mapper.SubjectMapper;
import com.bonc.dw3.mapper.SystemVariablesMapper;

@Service
@CrossOrigin(origins="*")
public class SubjectService {
	
	@Autowired
	SubjectMapper mapper;
	@Autowired
	SystemVariableService systemVariables;
	
	private static Logger log = LoggerFactory.getLogger(SubjectService.class);
	/**
	 * 1、最大账期
	 * @param id
	 * @return
	 */
	
	public Map<String,String> maxDate(String dateType){
		Map<String,String> date = new HashMap<>();
		String acctDay = systemVariables.acctTypeDay;//日账期
		String acctMonth = systemVariables.acctTypeMonth;
		if(dateType.equals(acctDay)){
			date = mapper.maxDate(dateType);
		}else if(dateType.equals(acctMonth)){
			date = mapper.maxDate(null);
		}else{
			date = null;
		}
		return date;
	}	
	
	/**
	 * 2、地域接口
	 * @param id
	 * @return
	 */
	public List<Map<String,Object>> area(){
		List<Map<String,String>> areaList = mapper.area();
		//找到所有的prov_id:31省+全国，放到provList里
		List<String> provList = new ArrayList<String>();
		for(Map<String,String> areaMap :areaList){
			//没读到一个map，将flag=false，表示是一个新的prov_id,还没有放到provList里
			boolean flag = false;
			if(provList != null && provList.size()>0){
				//倒序查找provList
				for(int i= provList.size()-1;i>=0;i--){
//				for(String prov :provList){
					String prov = provList.get(i);
					if(prov.equals(areaMap.get("PROV_ID"))){
						flag = true;//在provList中找到了一样的prov_id,跳过
						break;
					}
				}		
				if(flag==false){
					provList.add(areaMap.get("PROV_ID"));	
				}
			}else{
				provList.add(areaMap.get("PROV_ID"));
			}
		}
		
		List<Map<String,Object>> resList = new ArrayList<>();
		for(String pro:provList){
			Map<String,Object> provMap = new HashMap<>();
			provMap.put("proId", pro);
			
			List<Map<String,String>> cityList = new ArrayList<>();
			int i;
			for(i=0;i<areaList.size();i++){
				Map<String,String> areaMap = areaList.get(i);
				if(pro.equals(areaMap.get("PROV_ID"))){
				   provMap.put("proName", areaMap.get("PRO_NAME"));
				   
				   Map<String,String> cityMap = new HashMap<>();
				   cityMap.put("cityId", areaMap.get("AREA_ID"));
				   cityMap.put("cityName", areaMap.get("AREA_DESC"));
				   cityList.add(cityMap);
				   provMap.put("city", cityList);
				   
				   areaList.remove(areaMap);
				   i--;
				}
				
			}
			resList.add(provMap);
		}
		return resList;
	}
	
	/**
	 * 3、 查询条件
	 * @param
	 */
	public List<Map<String,Object>> getSelect(String markType){
		List<Map<String,String>> selectList = mapper.select(markType);
		//循环遍历查询结果selectList，找到唯一的tid（父id），写进keyList里
		List<String> keyList = new ArrayList<String>();
		for(Map<String,String> map : selectList){
			boolean flag = false;//每循环出一个map，都将flag值设为false，表示ksyList里面没有该值
			if(null!=keyList&&keyList.size()>0){
				for(String key:keyList){
					if(key.equals(map.get("TID"))){
						flag=true;
						break;
					}
				}
				//没有该值，存进去
				if(flag==false){
					keyList.add(map.get("TID"));	
					}
			}
			else{
				keyList.add(map.get("TID"));
			}
		}
		List<Map<String,Object>> resList = new ArrayList<Map<String,Object>>();
		//处理：循环遍历keyList，将相同父id的查询条件放进一个map里
		for(String tid:keyList){
			Map<String,Object> selectMap = new HashMap<String,Object>();
			selectMap.put("screenTypeId", tid);
		    List<Map<String,String>> dataList = new ArrayList<Map<String,String>>();
			for(Map<String,String> paramMap: selectList){
				if(paramMap.get("TID").equals(tid)){
					Map<String,String> dataMap = new HashMap<String,String>();
					dataMap.put("sid",paramMap.get("ID"));
					dataMap.put("sname", paramMap.get("TEXT"));
					dataList.add(dataMap);
					selectMap.put("screenTypeName", paramMap.get("TNAME"));
					
				}
			}
			selectMap.put("values", dataList);
			resList.add(selectMap);
		}
		return resList;
	}
	
    /**
     * 通过code模糊查询默认参数和code
     *
     * @Author gp
     * @Date 2017/5/3
     */
    public List<Map<String, String>> getInfosViaCode(String code){
        List<Map<String, String>> resultList = new ArrayList<>();
        resultList = mapper.getInfosViaCode(code);
        return resultList;
    }
    
	/**
	 * 4、模块选项卡接口
	 * @param markType
	 * @return
	 */
	public List<Map<String, String>> moduleTab(String markType) {
		List<Map<String, String>> resultList = new ArrayList<>();
        List<Map<String, String>> dataList = mapper.getModuleTab(markType);
        
        for(Map<String,String> map:dataList){
        	Map<String,String> dataMap = new HashMap<>();
        	dataMap.put("tabId", map.get("TABID"));
        	dataMap.put("tabName", map.get("TABNAME"));
        	resultList.add(dataMap);
        }
        
        return resultList;
	}

	/**
	 * 5、专题表格接口
	 * @param provinceId
	 * @param cityId
	 * @param date
	 * @param dimension
	 * @param moduleId
	 * @param markType
	 * @param dateType
	 * @return
	 * @throws ParseException 
	 */
	public Map<String,Object> getkpiData(String provinceId, String cityId, String date,
			List<Map<String,List<String>>> dimension, String moduleId, String markType, String dateType) throws ParseException{
		//获得最大账期
		Map<String, String> dateMap = maxDate(dateType);
		String maxDate = dateMap.get("ID");
		Map<String,Object> paramMap = paramProcess(maxDate,provinceId, cityId, date, dimension, moduleId, markType, dateType);
		
		String dateStr = date.replaceAll("-","");

		Map<String,Object> res = new HashMap<>();
		//获取title
		List<String> title = mapper.title(markType);
		log.info("表格title："+title);
		res.put("thData", title);
		//指标树结构
		List<Map<String,Object>> kpiTree = mapper.selectKpiTree(moduleId);
		//数据
		List<Map<String,Object>> resList = new ArrayList<>();
		List<Map<String,Object>> dataList = new ArrayList<>();
		
		//是最大账期,查两次库
		if(date.equals(maxDate)){
			//先从基础表查6天
	        paramMap.put("minDate", paramMap.get("sixDate"));
	        paramMap.put("date", paramMap.get("oneDate"));
			if(dateType.equals(systemVariables.acctTypeDay)){
				dataList = mapper.selectDay(paramMap);
				paramMap.put("table", "V_DM_KPI_D_0010");
				paramMap.put("minDate",date.replace("-", ""));
				paramMap.put("date",date.replace("-", ""));
				dataList.addAll(mapper.selectDay(paramMap));
			}else{
				dataList = mapper.selectMonth(paramMap);
				paramMap.put("table", "V_DM_KPI_M_0010");
				paramMap.put("minDate", date.replace("-", ""));
				paramMap.put("date", date.replace("-", ""));
				dataList.addAll(mapper.selectMonth(paramMap));
			}			
		}else{
			if(dateType.equals(systemVariables.acctTypeDay)){
				dataList = mapper.selectDay(paramMap);
			}else{
				dataList = mapper.selectMonth(paramMap);
			}
		}
		int days = 7;
		//组合
		for(Map<String,Object> kpiTreeMap : kpiTree){
			Map<String,Object> resMap = new HashMap<>();
			String kid = kpiTreeMap.get("KPI_CODE").toString();
			//1、根据指标id分组，组合对应的7天值
			List<Map<String, Object>> processList = kpiGroup(date,dataList,kid,days,dateType);
//			log.info("processList:"+processList);
    		//2、处理7天的数据、获取kpiValues、lineChartData、histogramData、ringRatio、identicalRatio字段
			//构造lineChartData字段
			Map<String,List<String>> lineChartData = new HashMap<>();
			//构造histogramData字段
			Map<String,List<String>> histogramData = new HashMap<>();
			//数字1-7
			List<String> xData = new ArrayList<>();
			for(int i=1;i<=days;i++){
				xData.add(Integer.toString(i));
			}
			List<String> lineData = new ArrayList<>();
			List<String> barData = new ArrayList<>();
    		for(Map<String,Object> map : processList){
    			
				Map<String,Object> kpiValueMap = dealData(kpiTreeMap, map);
				List<String> values = (List<String>) kpiValueMap.get("kpiValues");
				
    			//构造kpiValues,identicalRatio,ringRatio字段
    			if(dateStr.equals(map.get("date"))){
    				resMap.put("kpiValues", values);
    				resMap.put("ringRatio", kpiValueMap.get("ringRatio"));
    				resMap.put("identicalRatio", kpiValueMap.get("identicalRatio"));
    			}
    			
    			List<String> value = graphformat(kpiTreeMap, map);
    			lineData.add(value.get(0));
    			barData.add(value.get(1)); 
    		}
    		
			lineChartData.put("xData", xData);	
			lineChartData.put("value", lineData );
			
			histogramData.put("xData", xData);	
			histogramData.put("value", barData);
			
			resMap.put("histogramData", histogramData);
			resMap.put("lineChartData", lineChartData);
			resMap.put("kpiId",kid);
			resMap.put("unit", kpiTreeMap.get("UNIT"));
			resMap.put("kpiName", kpiTreeMap.get("KPI_NAME"));
    		resList.add(resMap);
		}
//		log.info("resList:"+resList);
		res.put("tbodyData", resList);
		return res;
	}

	/**
	 * 6、专题图标接口
	 * @param param
	 * @return
	 */
	public List<Map<String,String>> getIcons(String params) {
		List<Map<String,String>> resList = new ArrayList<>();
		if(!params.equals("no")){
			String[] markType = params.split(",");
			List<Map<String,String>> dataList = mapper.getIcons(markType);
			for(Map<String,String> dataMap :dataList){
				Map<String,String> resMap = new HashMap<>();
				resMap.put("id", dataMap.get("SUBJECT_CODE"));
				resMap.put("src", dataMap.get("ICON"));
				resList.add(resMap);
			}			
		}
		return resList;
	}	
	/**
	 * 输出数据处理
	 * @param kpiTree 指标树结构map
	 * @param mData   数据map
	 * @param date	     月账期
	 * @return
	 * @throws ParseException
	 */
    private Map<String,Object> dealData(Map<String,Object> kpiTree, Map<String,Object> mData) {
    	Map<String,Object> kpiValueMap = new HashMap<>();
    	//1、计算环比、同比
    	Map<String,Object> values = formula(mData);
    	kpiValueMap.put("ringRatio", values.get("ringRatio"));
    	kpiValueMap.put("identicalRatio", values.get("identicalRatio"));
    	//2、格式化输出：主要是给当月值和本年累计值加单位，以及对4个数据顺序输出
    	kpiValueMap.put("kpiValues", format(kpiTree,values));
    	return kpiValueMap;
    }
    
    /**
     * 计算环比、同比
     * @param mData 数据map
     * @return 处理后的数据map
     */
	private Map<String,Object> formula(Map<String, Object> mData){
		Map<String,Object> valueMap =new HashMap<>();
		DecimalFormat    df   = new DecimalFormat("######0.00");
//     1、获得基础数据kylin
//		Double dr = (Double)mData.get("dr");
//		Double zr = (Double) mData.get("zr");
//		Double sytq = (Double) mData.get("sytq");
//		Double bylj = (Double) mData.get("bylj");
//	    1、获得基础数据oracle
		double dr = (double) mData.get("dr");
		double zr = (double) mData.get("zr");
		double sytq = (double) mData.get("sytq");
		double bylj = (double) mData.get("bylj");
//		1、计算环比：(当日指标值-昨日指标值)/昨日指标值
    	if(zr==0){
    		valueMap.put("hb", "-");
    		valueMap.put("ringRatio", "0");
    	}else{
    		double hb = (dr-zr)/Math.abs(zr)*100;
    		if(hb>0){
    			valueMap.put("ringRatio", "1");
    		}else if(hb == 0){
    			valueMap.put("ringRatio", "0");
    		}else{
    			valueMap.put("ringRatio", "-1");
    		}
    		String hbStr = df.format(hb)+"%";
    		valueMap.put("hb", hbStr);
    	}
//		2、计算同比：(当日指标值-上月同期指标值)/上月同期指标值
    	if(sytq==0){
    		valueMap.put("tb", "-");
    		valueMap.put("identicalRatio", "0");
    	}else{
    		double tb = (dr-sytq)/Math.abs(sytq)*100;
    		if(tb>0){
    			valueMap.put("identicalRatio", "1");
    		}else if(tb == 0){
    			valueMap.put("identicalRatio", "0");
    		}else{
    			valueMap.put("identicalRatio", "-1");
    		}    		
    		String tbStr = df.format(tb)+"%";
    		valueMap.put("tb", tbStr);
    	}
    	
    	valueMap.put("dr", dr);
    	valueMap.put("bylj", bylj);
    	return valueMap;
	}

	/**
	 * 处理单位：精确度和百分号  
	 * @param kpiTree  指标树结构map
	 * @param values 经过公示计算过的数据map
	 * @return 四个数据值的集合
	 */
	private List<String> format(Map<String, Object> kpiTree,
				Map<String, Object> values) {
		//1、先除 2、判断保留
			Double uatio = ((BigDecimal) kpiTree.get("UATIO")).doubleValue();
			String format = (String) kpiTree.get("FORMAT"); 
			Double dr =  (Double)values.get("dr");
			DecimalFormat  df = new DecimalFormat("######0.00");
			DecimalFormat  dm = new DecimalFormat("######0");
			dr = dr/uatio;
			String drz ="";
			String byljz ="";
			String unit="";
			if(null != kpiTree.get("UNIT")){
				unit = (String) kpiTree.get("UNIT");
			}
			//处理本月累计值
			if(null != values.get("bylj")){
				Double bylj = (Double) values.get("bylj");
				if(bylj!=0){
					bylj = bylj/uatio;
					if(!StringUtils.isBlank(format)&&"FM9999999999990.00".equals(format)){
						byljz = df.format(bylj);
					}else{
						byljz = dm.format(bylj);
					}
					//当月值是否加%
					if(!StringUtils.isBlank(unit)&&unit.equals("%")){
						byljz = byljz+"%";
					}
					values.put("bylj", byljz);
				}else{
					values.put("bylj", "-");
				}
			}
			//当日值精确度
			if(!StringUtils.isBlank(format)&&"FM9999999999990.00".equals(format)){
				drz = df.format(dr);
			}else{
				drz = dm.format(dr);
			}
			//当月值是否加%
			if(!StringUtils.isBlank(unit)&&unit.equals("%")){
				 drz = drz+"%";
			}
			values.put("dr", drz);
			List<String> value = new ArrayList<>();
			value.add((String) values.get("dr"));
			if(null != values.get("bylj")){
				value.add((String) values.get("bylj"));
			}
			//判断null，是针对图表返回的数据
			if(null != values.get("hb")){
				value.add((String) values.get("hb"));
			}
			if(null != values.get("tb")){
				value.add((String) values.get("tb"));
			}
			return value;
		}
	
	
	/**
	 * 处理图表单位：精确度
	 * @param kpiTree  指标树结构map
	 * @param values 经过公示计算过的数据map
	 * @return 四个数据值的集合
	 */
	private List<String> graphformat(Map<String, Object> kpiTree,
				Map<String, Object> values) {
			Double uatio = ((BigDecimal) kpiTree.get("UATIO")).doubleValue();
			String format = (String) kpiTree.get("FORMAT"); 
			DecimalFormat  df = new DecimalFormat("######0.00");
			DecimalFormat  dm = new DecimalFormat("######0");			
			Double dr =  (Double)values.get("dr");
			Double bylj = (Double) values.get("bylj");
			String drz = "";
			String byljz = "";
			//处理当日值
			dr = dr/uatio;
			//处理本月累计值
			bylj = bylj/uatio;
			//本月累计值精确度
			if(!StringUtils.isBlank(format)&&"FM9999999999990.00".equals(format)){
				byljz = df.format(bylj);
				drz = df.format(dr);
			}else{
				byljz = dm.format(bylj);
				drz = dm.format(dr);
			}
			List<String> value = new ArrayList<>();
			value.add(drz);
			value.add(byljz);
			return value;
		}
	/**
	 * 根据kpi分组
	 * @param date
	 * @param dataList
	 * @param kid
	 * @return
	 * @throws ParseException
	 */
	private List<Map<String, Object>> kpiGroup(String date,
			List<Map<String, Object>> dataList,String kid,int days,String dateType)
			throws ParseException {
		//数据表中对应的数据一天也没有
		List<Map<String,Object>> kpiMapList = new ArrayList<>();
		for(int i=0;i<dataList.size();i++){
			Map<String,Object> dataMap = dataList.get(i);
			//数据表中存在该指标
			if(!StringUtils.isBlank(kid)&&kid.equals(dataMap.get("KPI_CODE"))){
				
				Map<String,Object> kpiMap = new HashMap<>();
				kpiMap.put("date", dataMap.get("ACCT_DATE"));
				kpiMap.put("dr", ((BigDecimal) dataMap.get("DR")).doubleValue());
            	kpiMap.put("bylj", ((BigDecimal) dataMap.get("BYLJ")).doubleValue());
            	kpiMap.put("zr", ((BigDecimal) dataMap.get("ZR")).doubleValue());
            	kpiMap.put("sytq", ((BigDecimal) dataMap.get("SYTQ")).doubleValue());
				kpiMapList.add(kpiMap);
				dataList.remove(dataMap);
			   	i--;    				
			}
		}
		//处理7天数据
		List<Map<String,Object>> processList = processData(kpiMapList,date,kid,days,dateType);
		return processList;
	}
	
	/**
	 * 处理7天数据
	 * @param kpiMapList
	 * @param date
	 * @param kid
	 * @return
	 * @throws ParseException
	 */
	public List<Map<String,Object>> processData(List<Map<String, Object>> kpiMapList,String date,String kid,
			int days,String dateType) throws ParseException {
		List<Map<String,Object>> resList = new ArrayList<>();
		SimpleDateFormat sdf = null;
		if(dateType.equals(systemVariables.acctTypeDay)){
			 sdf=new SimpleDateFormat("yyyyMMdd");//小写的mm表示的是分钟  
		}else{
			sdf=new SimpleDateFormat("yyyyMM");//小写的mm表示的是分钟  
		}
        for(int i=days;i>0;i--){
    		String str = DateUtils.getDate(date, "1",dateType);
    		Date d1 = sdf.parse(str);
            Calendar cal = Calendar.getInstance();
            cal.setTime(d1);
        	if(dateType.equals(systemVariables.acctTypeDay)){
        		cal.add(Calendar.DATE, -i);
        	}else{
        		cal.add(Calendar.MONTH, -i);
        	}          
          Date da = cal.getTime();
          String dateStr = sdf.format(da);
			//没有对应的日期
			int flag = 0;
	  		for(Map<String,Object> map : kpiMapList){
	  			if(dateStr.equals(map.get("date"))){
	  				resList.add(map);
					flag = 1;
				}
	  		}
	  		if(flag == 0){
	  			double value = 0.0;
	  			Map<String,Object> map = new HashMap<>();
	  			map.put("date", dateStr);
	  			map.put("dr", value);
	  			map.put("bylj", value);
            	map.put("zr", value);
            	map.put("sytq", value);
	  			resList.add(map);
	  		}
        }
        return resList;
	}

	/**
	 * 参数处理
	 * @param provinceId
	 * @param cityId
	 * @param date
	 * @param dimension
	 * @param moduleId
	 * @param markType
	 * @param dateType
	 * @return
	 * @throws ParseException 
	 */
	public Map<String,Object> paramProcess(String maxDate,String provinceId, String cityId, String date,
			List<Map<String,List<String>>> dimension, String moduleId, String markType, String dateType) throws ParseException{

		Map<String,Object> paramMap = new HashMap<>();
		//选择某个地势
		if(!cityId.equals("-1")){
			paramMap.put("city", cityId);
			paramMap.put("prov", null);
		}else{
			paramMap.put("city", "-1");
			paramMap.put("prov", provinceId);
		}
		paramMap.put("moduleId", moduleId);

		//账期
        String sixDate = DateUtils.getDate(date, "-6",dateType);
        String oneDate = DateUtils.getDate(date, "-1",dateType);
        
		//默认传基础表
		if(dateType.equals(systemVariables.acctTypeDay)){
			paramMap.put("table", "V_DM_KPI_D_0010");
		}else{
			paramMap.put("table", "V_DM_KPI_M_0010");
		}
		//维度处理
		List<Map<String,Object>> dimensions = dimensionProcess(dimension, markType);
		paramMap.put("dimensions", dimensions);
		paramMap.put("sixDate", sixDate);
        paramMap.put("oneDate", oneDate);
        paramMap.put("minDate", sixDate);
		paramMap.put("date", date.replaceAll("-", ""));
		return paramMap;
	}

	/**
	 * 维度处理
	 * @param dimension
	 * @param markType
	 * @return
	 */
	private List<Map<String,Object>> dimensionProcess(List<Map<String, List<String>>> dimension,
			String markType) {
		//维度处理
		//1、从库中查到维度列表
		List<Map<String, String>> selectList = mapper.select(markType);
		List<Map<String,Object>> dimensions = new ArrayList<>();
		//循环遍历维度参数
		for(Map<String,List<String>> map : dimension){
			Map<String,Object> dimensionMap = new HashMap<>();
			Map<String,Object> dimensionsMap = new HashMap<>();
			Set<String> keys = map.keySet();
			
			for(String key :keys){
				String channel_type = null;
				String query_sql = null;
				String[] temp = map.get(key).toString().replaceAll("\\[", "").replaceAll("]", "").split(",");
				List<String> values = new ArrayList<>();
				//去空格
				for(String value : temp){
					values.add(value.trim());
				}
				
				//1、判断列表里的参数个数
				if(values.size()==1){
					//判断是否为全部 all
					String id = values.get(0);
					for(Map<String,String> selectMap :selectList){
						if(!StringUtils.isBlank(selectMap.get("QUERYSQL"))){
							query_sql = selectMap.get("QUERYSQL");
						}else{
							query_sql = "false";
						}
						channel_type = selectMap.get("CHANNEL_TYPE");
						if(key.equals(selectMap.get("TID"))
								&&id.equals(selectMap.get("ID"))
								&&"all".equals(selectMap.get("IS_ALL"))){
							dimensionMap.put(channel_type, null);
							break;
						}else if(key.equals(selectMap.get("TID"))){
							dimensionsMap.put(query_sql, values);
							dimensionMap.put(channel_type, dimensionsMap);
							break;
						}
					}
				}else{
					for(Map<String,String> selectMap :selectList){
						if(key.equals(selectMap.get("TID"))){
							if(!StringUtils.isBlank(selectMap.get("QUERYSQL"))){
								query_sql = selectMap.get("QUERYSQL");
							}else{
								query_sql = "false";
							}							
							channel_type = selectMap.get("CHANNEL_TYPE");
							break;
						}
					}
					dimensionsMap.put(query_sql, values);
					dimensionMap.put(channel_type, dimensionsMap);
				}
			}

			dimensions.add(dimensionMap);
		}
		return dimensions;
	}
}



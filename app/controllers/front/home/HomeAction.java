package controllers.front.home;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.t_content_advertisements;
import models.t_content_news;
import net.sf.json.JSONObject;

import org.apache.commons.lang.StringUtils;

import utils.Arith;
import utils.ErrorInfo;
import business.Ads;
import business.AdsEnsure;
import business.AdsPartner;
import business.BackstageSet;
import business.News;
import business.NewsType;
import business.newr.Bid.Repayment;
import business.newr.Bill;
import business.newr.Invest;
import constants.Constants;
import constants.OptionKeys;
import controllers.BaseController;

/**
 * 
 * @author liuwenhui
 *
 */
public class HomeAction extends BaseController{
	
	
	
	/*网站首页*/
	public static void home(){
		
		ErrorInfo error = new ErrorInfo();
				
		List<t_content_advertisements> homeAds = Ads.queryAdsByLocation(Constants.HOME_PAGE_PC, error); // 广告条
		String apr = params.get("apr");
 		String amount = params.get("amount");
 		String loanSchedule = params.get("loanSchedule");
 		String loanType = params.get("loanType");
 		String minLevel = params.get("minLevel");
 		String keywords = params.get("keywords");
 		
 		Map<String, Object> conditionMap=new HashMap<String, Object>();
		conditionMap.put("apr", apr);
		conditionMap.put("amount", amount);
		conditionMap.put("loanSchedule", loanSchedule);
		conditionMap.put("loanType", loanType);
		conditionMap.put("minLevel", minLevel);
		conditionMap.put("keywords", keywords);
		
	
		List<t_content_news> investSkills = News.queryNewForFront(10l, 5, error) ;//首页借款技巧
		
		List<t_content_news> loanSkills = News.queryNewForFront(11l, 5, error) ;//首页理财技巧
		
		List<t_content_news> news = News.queryNewForFront(7l, 5, error) ;//首页官方公告
		
		
		List<Map<String,String>> maps = Invest.queryNearlyInvest(error);
		
		List<AdsEnsure> adsEnsure = AdsEnsure.queryEnsureForFront(error); //四大安全保障
		
		List<AdsPartner>  adsPartner = AdsPartner.qureyPartnerForFront(error);//合作伙伴
		
		List<NewsType> types = NewsType.queryChildTypes(1, error);
		
		
	
		render(homeAds, null,null,null,null,investSkills,loanSkills,news, 
				null, adsEnsure, adsPartner, types ,maps,conditionMap);
	}
	
	/**
	 * 还款计算器
	 */
	public static void wealthToolkitRepaymentCalculator(){
		List<Repayment> rtypes = Repayment.queryRepaymentType(null, new ErrorInfo()); // 还款类型
		
		render(rtypes);
	}
	
	/**
	 * 还款明细(异步)
	 */
	public static void repaymentCalculate(double amount, double apr, int period, int periodUnit, int repaymentType){
		List<Map<String, Object>> payList = null;
		
		payList = Bill.repaymentCalculate(amount, apr, period, periodUnit, repaymentType);
		
		render(payList);
	}
	
	/**
	 * 净值计算器
	 */
	public static void wealthToolkitNetValueCalculator(){
		ErrorInfo error = new ErrorInfo();
		render(null);
	}
	
	/**
	 * 利率计算器
	 */
	public static void wealthToolkitAPRCalculator(){
		ErrorInfo error = new ErrorInfo();
		
		List<Repayment> rtypes = Repayment.queryRepaymentType(null, error); // 还款类型
		
		String value = OptionKeys.getvalue(OptionKeys.CREDIT_LIMIT, error); // 得到积分对应的借款额度值
		double serviceFee = StringUtils.isBlank(value) ? 0 : Double.parseDouble(value);

		render(rtypes, serviceFee);
	}
	
	/**
	 * 利率计算器,计算年华收益、总利益(异步)
	 */
	public static void aprCalculator(double amount, double apr,int repaymentType,double award,int rperiod){
		ErrorInfo error = new ErrorInfo();
		DecimalFormat df = new DecimalFormat("#.00");
		
		double managementRate = BackstageSet.getCurrentBackstageSet().investmentFee / 100;//系统管理费费率
		double earning = 0;
		
		if(repaymentType == 1){/* 按月还款、等额本息 */
			double monRate = apr / 12;// 月利率
			int monTime = rperiod;
			double val1 = amount * monRate * Math.pow((1 + monRate), monTime);
			double val2 = Math.pow((1 + monRate), monTime) - 1;
			double monRepay = val1 / val2;// 每月偿还金额
			
			/**
			 * 年化收益
			 */
			 earning = Arith.excelRate((amount - award),
					Double.parseDouble(df.format(monRepay)), monTime, 200, 15)*12*100;
			 earning = Double.parseDouble(df.format(earning)+"");
		}
		
		if(repaymentType == 2 || repaymentType == 3){ /* 按月付息、一次还款   */
			double monRate = apr / 12;// 月利率
			int monTime = rperiod;// * 12;借款期限填月
			double borrowSum = Double.parseDouble(df.format(amount));
			double monRepay = Double.parseDouble(df.format(borrowSum * monRate));// 每月偿还金额
			double allSum = Double.parseDouble(df.format((monRepay * monTime)))
					+ borrowSum;// 还款本息总额
			 earning = Arith.rateTotal(allSum,
					(borrowSum - award), monTime)*100;
			 earning = Double.parseDouble(df.format(earning)+"");
		}
		
		
		JSONObject obj = new JSONObject();
		obj.put("managementRate", managementRate < 0 ? 0 : managementRate); 
		obj.put("earning", earning); 
		
		renderJSON(obj);
	}
	
	/**
	 * 服务手续费
	 */
	public static void wealthToolkitServiceFee(){
		ErrorInfo error = new ErrorInfo();
		String content = News.queryContent(-1011L, error);
		flash.error(error.msg);
		
		renderText(content);
	}
	

}

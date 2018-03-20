package controllers.newr.front.account;


import java.util.List;
import java.util.Map;

import javax.persistence.Query;

import models.newr.v_recommedFeeList;
import models.newr.v_recommendFeeDetail;
import play.db.jpa.JPA;
import play.mvc.With;
import utils.ErrorInfo;
import utils.NumberUtil;
import utils.PageBean;
import business.newr.Invest;
import business.newr.User;
import controllers.interceptor.newr.FInterceptor;
import controllers.newr.BaseController;
import controllers.newr.SubmitRepeat;

/**
 * 
 * @author cp
 *
 */
@With({FInterceptor.class, SubmitRepeat.class})
public class AccountHome extends BaseController {
	/**
	 * 我的账户的首页
	 */
	public static void home() {
		//避免缓存中的数据与数据库一致
		User user = new User();
		user.createBid=true;
		user.id = User.currUser().id;
		render(user);
	}
	
	

	
	public static void queryRecommendFeeReport(){
		int currPage =1 ;
		int pageSize = 8;
		String currPageStr = params.get("currPage");
		String pageSizeStr = params.get("pageSize");
		if(NumberUtil.isNumericInt(currPageStr)) {
			currPage = Integer.parseInt(currPageStr);
 		}
		
 		if(NumberUtil.isNumericInt(pageSizeStr)) {
 			pageSize = Integer.parseInt(pageSizeStr);
 		}
 		ErrorInfo error = new ErrorInfo();
	    Map<String,Object> outlineData = Invest.obtainRecommendFeeOutline(User.currUser().id, error);
	    PageBean<v_recommedFeeList> page = Invest.obtainDecommendFeeList(error, pageSize, currPage, User.currUser().id);
	    render(outlineData,page);
	}
	public static void queryRecommendFeeDetail(String userId){
		ErrorInfo error = new ErrorInfo();
		long useId = Long.parseLong(userId);
		List<v_recommendFeeDetail> list =Invest.obtainRecommendFeeDetail(User.currUser().id, useId, error);
		renderJSON(list);
	}
}

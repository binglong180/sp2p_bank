package controllers.newr.front.invest;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;

import models.t_bids;
import models.t_bill_invests;
import models.t_bills;
import models.t_dict_ad_citys;
import models.t_dict_ad_provinces;
import models.newr.t_users;

import org.apache.commons.lang.StringUtils;

import payment.newr.PaymentProxy;
import play.cache.Cache;
import play.libs.Codec;
import play.mvc.Before;
import utils.CaptchaUtil;
import utils.ErrorInfo;
import utils.NumberUtil;
import utils.PageBean;
import utils.Security;
import business.newr.Bid;
import business.newr.Invest;
import business.newr.User;

import com.shove.security.Encrypt;

import constants.newr.Constants;
import controllers.newr.BaseController;
import controllers.newr.front.account.LoginAndRegisterAction;

/**
 * 
 * @author liuwenhui
 *
 */
public class InvestAction extends BaseController{
	@Before(only={"newr.front.invest.InvestAction.addAutoInvest"	
	})
	public static void checkLogin(){
		String sign = params.get("id");
		long id = Security.checkSign(sign, Constants.USER_ID_SIGN, 60*60*12, new ErrorInfo());
		User user = null;
		if(id > 0){
			user = User.currAppUser(id+"");
			//来自于APP的webview访问并设更新户信息
			User.setCurrUser(user);
		}else{
		    user = User.currUser();
			
			if(user == null){
				LoginAndRegisterAction.login(null);
			}
		}
		
		renderArgs.put("user", user);
	}
	
	   public static void investSuccess(){
		 render();  
	   }
       public static void investHome(){
		ErrorInfo error = new ErrorInfo();

		int currPage = Constants.ONE;
		int pageSize = Constants.TEN;
		
		String currPageStr = params.get("currPage");
		String pageSizeStr = params.get("pageSize");
		
		if(NumberUtil.isNumericInt(currPageStr)) {
			currPage = Integer.parseInt(currPageStr);
 		}
		
 		if(NumberUtil.isNumericInt(pageSizeStr)) {
 			pageSize = Integer.parseInt(pageSizeStr);
 		}
 		
 		String apr = params.get("apr");
 		String amount = params.get("amount");
 		String loanSchedule = params.get("loanSchedule");
 		String startDate = params.get("startDate");
 		String endDate = params.get("endDate");
 		String loanType = params.get("loanType");
 		String minLevel = params.get("minLevel");
 		String maxLevel = params.get("maxLevel");
 		String orderType = params.get("orderType");
 		String keywords = params.get("keywords");
 		
 /*		
		
		PageBean<v_front_all_bids>  pageBean = new PageBean<v_front_all_bids>();
		pageBean= Invest.queryAllBids(Constants.SHOW_TYPE_1, currPage,pageSize, apr, amount, loanSchedule, startDate, endDate, loanType, minLevel,maxLevel,orderType,keywords,error);
		
		if(error.code < 0){
			render(Constants.ERROR_PAGE_PATH_FRONT);
		}
		
		render(list,null,products,pageBean);*/
	}
	
	/**
	 * 立即投资
	 * */
	public static void immediatelyInvest(long bidId, String showBox){
		
		ErrorInfo error = new ErrorInfo();
		Bid bid = new Bid();
		bid.id=bidId;
		t_users t2_users = new t_users();
		t2_users = t2_users.find("id=?", bid.userId).first();
		User user = User.currUser();
		String uuid = CaptchaUtil.getUUID(); // 防重复提交UUID
		boolean flag = false;		
		if(StringUtils.isNotBlank(showBox)){
			showBox = Encrypt.decrypt3DES(showBox, bidId + Constants.ENCRYPTION_KEY);
			
			if(showBox.equals(Constants.SHOW_BOX))
				flag = true;
		}
		
		int status = Constants.INVEST_DETAIL;
	   
/*		PageBean<v_invest_records> pageBean = new PageBean<v_invest_records>();
		pageBean = Invest.queryBidInvestRecords(bidId,error);
		render(bid,null,null,user,status,pageBean,null,uuid);*/
	}
	

	
	/**
	 * 完善基本资料
	 */
	private static void addBaseInfo(){
		List<t_dict_ad_provinces> provinces = (List<t_dict_ad_provinces>) Cache.get("provinces"); // 省

		String key = "province" + session.getId();
		Object obj = Cache.get(key);
		Cache.delete(key);
		int province = obj == null ? 1 : Integer.parseInt(obj.toString());
		List<t_dict_ad_citys> cityList = User.queryCity(province); // 市
		
		boolean ips_enable = Constants.IPS_ENABLE;
		boolean check_msg_code = Constants.CHECK_PIC_CODE;
		
		String randomID = Codec.UUID();
		
		renderArgs.put("provinces", provinces);

		renderArgs.put("cityList", cityList);
		renderArgs.put("ips_enable", ips_enable);
		renderArgs.put("check_msg_code", check_msg_code);
		renderArgs.put("randomID", randomID);
	}
    public static void investList(){
    	ErrorInfo error = new ErrorInfo();
    	int currPage =1 ;
		int pageSize = 12;
		String currPageStr = params.get("currPage");
		String pageSizeStr = params.get("pageSize");
		if(NumberUtil.isNumericInt(currPageStr)) {
			currPage = Integer.parseInt(currPageStr);
 		}
		
 		if(NumberUtil.isNumericInt(pageSizeStr)) {
 			pageSize = Integer.parseInt(pageSizeStr);
 		}
/*		PageBean<v_front_all_bids>  pageBean = new PageBean<v_front_all_bids>();
		pageBean= Invest.queryAllBids(Constants.SHOW_TYPE_1, currPage,pageSize,error);
		render(pageBean);*/
    }
    
	/**
	 * 确认投标
	 * @param bidId
	 */
	public static void confirmInvest(String sign, String uuid){
		User user = User.currUser();
		
		if(null == user) 
			LoginAndRegisterAction.login(null);

		ErrorInfo error = new ErrorInfo();
		
		long bidId = Security.checkSign(sign, Constants.BID_ID_SIGN, Constants.VALID_TIME, error);
		
		if(bidId < 1){
			flash.error(error.msg); 

			immediatelyInvest(bidId, "");
		}
		
		/* 防重复提交 */
		if(!CaptchaUtil.checkUUID(uuid)){
			flash.error("请求已提交或请求超时!");
			
			immediatelyInvest(bidId, "");
		}
		
		String investAmountStr = params.get("investAmount").trim();
		String dealpwd = params.get("dealpwd");
		
		if(StringUtils.isBlank(investAmountStr)){
			error.msg = "投标金额不能为空！";
			flash.error(error.msg);
			immediatelyInvest(bidId, "");
		}
		if(Double.parseDouble(investAmountStr)<Constants.MININVESTMONEY){
			error.msg = "投标金额不能低于"+Constants.MININVESTMONEY+"！";
			flash.error(error.msg);
			immediatelyInvest(bidId, "");
		}
		boolean b=investAmountStr.matches("^[1-9][0-9]*$");
    	if(!b){
    		error.msg = "对不起！只能输入正整数!";
    		flash.error(error.msg);
    		immediatelyInvest(bidId, "");
    	} 
    	
    	int investAmount = Integer.parseInt(investAmountStr);
		Invest.invest(user.id, bidId, investAmount,  Constants.CLIENT_PC, error);
		
    	if (error.code == Constants.BALANCE_NOT_ENOUGH) {
			flash.put("code", error.code);
			flash.put("msg", error.msg);
			
			immediatelyInvest(bidId, "");
		}
    	
		if (error.code < 0) {
			flash.error(error.msg);
			immediatelyInvest(bidId, "");
		}
		
		Map<String, String> bid = Invest.bidMap(bidId, error);

		if (error.code < 0) {
			flash.error("对不起！系统异常！请您联系平台管理员！");
			immediatelyInvest(bidId, "");
		}
		
		if (Constants.IPS_ENABLE) {
			if (error.code < 0) {
				flash.error(error.msg);
				immediatelyInvest(bidId, "");
			}
			
			
			//调用托管接口
			//中金-市场订单支付
			  Map<String, Object> resultMap =PaymentProxy.getInstance().investSMS(error, Constants.PC, t_bids.findById(bidId), user, investAmount);
			  
			 String orderNo= (String) resultMap.get("orderNo");
			 String paymentNo = (String) resultMap.get("paymentNo");
			 String txSNBing = (String) resultMap.get("txSNBinding");
			 flash.error(error.msg);
			
		}

		if(error.code > 0){
			flash.put("amount", NumberUtil.amountFormat(investAmount));
			String showBox = Encrypt.encrypt3DES(Constants.SHOW_BOX, bidId + Constants.ENCRYPTION_KEY);
			immediatelyInvest(bidId, showBox);
		}else{
			flash.error(error.msg);
			immediatelyInvest(bidId, "");
		}
	}
	
	
	
	/**
	 * 投标记录分页ajax方法
	 * @param pageNum
	 * @param pageSize
	 * @param bidId
	 */
	public static void viewBidInvestRecords(int pageNum, int pageSize){
		
		ErrorInfo error = new ErrorInfo();
	    int currPage = pageNum;
		int pageSizeT = pageSize ;
		if(params.get("currPage")!=null) {
			currPage = Integer.parseInt(params.get("currPage"));
		}
		if(error.code < 0){
			render(Constants.ERROR_PAGE_PATH_FRONT);
		}
		
		PageBean<models.newr.v_newr_invest_records> pageBean = new PageBean<models.newr.v_newr_invest_records>();
		if(User.currUser()!=null){
			pageBean = Invest.queryBidInvestRecords(currPage,  pageSizeT, User.currUser().id,error);
		}else{
			redirect("/newr/front/home");
		}
		
		
		if(error.code < 0){
			render(Constants.ERROR_PAGE_PATH_FRONT);
		}
		render(pageBean);
		
	}


	/**
	 * 投资合同
	 * */
	public static void investContract(models.newr.v_newr_invest_records records){
		DateFormat timeFormat=new SimpleDateFormat("yyyy年MM月dd日");
		String contractDate = timeFormat.format(records.time);
		Double invest_amount = records.invest_amount;
		Double amount = (records.unreceive==null?0.00:records.unreceive) + (records.receive==null?0.00:records.receive);
		
//		records.bidId = new Long((long)15);
		Bid bid = new Bid();
		bid.id=records.bidId;

		
		t_bills t_bills = new t_bills();
//		t_bills = t_bills.find("bid_id=?", records.bidId).first();
		t_bills = t_bills.find("bid_id=? and periods = (select max(periods) from t_bills where bid_id = ?)", records.bidId, records.bidId).first();
		DateFormat timeFormat2=new SimpleDateFormat("yyyy-MM-dd");
		String repayment_time = timeFormat2.format(t_bills.repayment_time);
		
		t_bill_invests t_bill_invests = new t_bill_invests();
		t_bill_invests = t_bill_invests.find("bid_id=?", records.bidId).first();
		String receive_time = timeFormat2.format(t_bill_invests.receive_time);
		
		t_users t2_user = new t_users();
		t2_user = t_users.find("id=?", bid.userId).first();
		
		render(bid,null,null,contractDate,invest_amount,amount,repayment_time,receive_time,t2_user);
	}
	public static void investDetail(String title,long id){
		  String hql ="select  new Map(concat('车快融',title,'期') as title,periods as periods," +
	        		" (case when receive_corpus<=0 then receive_interest " +
	        		" when receive_interest<=0 then receive_corpus " +
	        		" else (receive_interest+receive_corpus) end) as amount,receive_time as receive_time," +
	        		" (case when status = -4 then '已还' when status = -1 then '未还' when status = 0 then '已还' " +
	        		" else '其他' END) as status ) from t_bill_invests b where user_id= ? and title ="+"'"+title+"' and invest_id = ?";
	         List<Map<String,Object>> result = t_bill_invests.find(hql, User.currUser().id,id).fetch();
	        renderJSON(result);
	}

}

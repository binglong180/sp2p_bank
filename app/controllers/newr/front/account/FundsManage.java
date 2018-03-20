package controllers.newr.front.account;


import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import models.t_content_news;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import play.mvc.With;
import utils.ErrorInfo;
import utils.PageBean;
import business.BackstageSet;
import business.DictBanksDate;
import business.News;
import business.Optimization.UserOZ;
import business.newr.User;
import business.newr.UserBankAccounts;
import constants.Constants;
import controllers.newr.BaseController;
import controllers.newr.SubmitRepeat;

@With({SubmitRepeat.class})
public class FundsManage extends BaseController {
	
	//普通key
	private static final String NORMAL_KEY = "name";
	
	//普通value
	private static final String NORMAL_VALUE = "value";

	
	/**
	 * 添加银行账号
	 */
	public static void addBank(int addBankCode, int addProviceCode, int addCityCode, String addBranchBankName, String addAccount, String addAccountName){
		User user = User.currUser();

		ErrorInfo error = new ErrorInfo();
		
		String bankName = DictBanksDate.queryBankByCode(addBankCode);
		String provice = DictBanksDate.queryProvinceByCode(addProviceCode);
		String city = DictBanksDate.queryCityByCode(addCityCode);
		
		UserBankAccounts bankUser =  new UserBankAccounts();
		
		bankUser.userId = user.id;
		bankUser.bankName = bankName;
		bankUser.bankCode = addBankCode;
		bankUser.provinceCode = addProviceCode;
		bankUser.cityCode = addCityCode;
		bankUser.branchBankName = addBranchBankName;
		bankUser.province = provice;
		bankUser.city = city;
		bankUser.account = addAccount;
		bankUser.accountName = addAccountName;
		
		bankUser.addUserBankAccount(error);
		
		JSONObject json = new JSONObject();
		json.put("error", error);
		
		renderJSON(json);
	}
	
	//保存银行账号
	public static void saveBank(){
		render();
	}
	
	/**
	 * 编辑银行账号
	 */

	public static void editBank(long editAccountId, int eidtBankCode, int eidtProviceCode, int eidtCityCode, String eidtBranchBankName, String editAccount, String editAccountName){
		ErrorInfo error = new ErrorInfo();
		
		String bankName = DictBanksDate.queryBankByCode(eidtBankCode);
		String provice = DictBanksDate.queryProvinceByCode(eidtProviceCode);
		String city = DictBanksDate.queryCityByCode(eidtCityCode);

		User user = User.currUser();
		UserBankAccounts userAccount = new UserBankAccounts();
		
		userAccount.bankName = bankName;
		userAccount.bankCode = eidtBankCode;
		userAccount.provinceCode = eidtProviceCode;
		userAccount.cityCode = eidtCityCode;
		userAccount.branchBankName = eidtBranchBankName;
		userAccount.province = provice;
		userAccount.city = city;
		userAccount.account = editAccount;
		userAccount.accountName = editAccountName;

		userAccount.editUserBankAccount(editAccountId, user.id, true, error);

		JSONObject json = new JSONObject();
		json.put("error", error);
		
		renderJSON(json);
	}
	
	/**
	 * 删除银行账号
	 */
	public static void deleteBank(long accountId){
		ErrorInfo error = new ErrorInfo();
		
		UserBankAccounts.deleteUserBankAccount(User.currUser().id, accountId, error);
		
		JSONObject json = new JSONObject();
		json.put("error", error);
		
		renderJSON(json);
	}
		
	/**
	 * 根据选择的银行卡id查询其信息
	 */
	public static void QueryBankInfo(long id){
		JSONObject json = new JSONObject();
		
		UserBankAccounts bank = new UserBankAccounts();
		bank.setId(id);
		
		json.put("bank", bank);
		
		renderJSON(json);
	}
	

	
	
	//交易详情
	public static void dealDetails(){
		render();
	}
	

	

	

	

	
	/**
	 * 通过省份code获取城市code-name
	 */
	public static void queryCityCode2NameByProvinceCode(int provinceCode){
		ErrorInfo error = new ErrorInfo();
		Map<String,String> cityMaps = DictBanksDate.queryCityCode2NameByProvinceCode(provinceCode, error);
		JSONArray array = buildJSONArrayByMaps(cityMaps);
		renderJSON(array);
	}
	
	/**
	 * 通过组合条件搜索银行支行名称(条件中最少需要提供银行code,否则数据库数据量查询缓慢,耗时会在6S左右检索出数据)
	 * @param cityCode
	 * @param bankCode
	 * @param searchValue
	 */
	public static void queryBankCode2NameByCondition(int cityCode,int bankCode,String searchValue){
		if (0 == cityCode || 0 == bankCode){
			return;
		}
		Map<String,Object> maps = new HashMap<String, Object>();
		maps.put("cityCode", cityCode);
		maps.put("bankCode", bankCode);
		maps.put("searchValue", searchValue);
		ErrorInfo error = new ErrorInfo();
		Map<String,String> bankMaps  = DictBanksDate.queryBankCode2NameByCondition(maps, error);
		JSONObject object = buildJSONObject(bankMaps);
		renderJSON(object);
	}
	
	private static JSONObject buildJSONObject(Map<String,String> maps){
		Set<Entry<String, String>> set = maps.entrySet();
		JSONArray array = new JSONArray();
		for(Entry<String, String> entry : set){
			JSONObject obj = new JSONObject();
			obj.put("title", entry.getValue());
			array.add(obj);
		}
		JSONObject object = new JSONObject();
		object.put("data", array);
		return object;
	}
	
	/**
	 * Map集合组装公用JSONArray(name-value键值对)
	 * @param maps
	 * @return
	 */
	private static JSONArray buildJSONArrayByMaps(Map<String,String> maps){
		Set<Entry<String, String>> set = maps.entrySet();
		JSONArray array = new JSONArray();
		for(Entry<String, String> entry : set){
			JSONObject obj = new JSONObject();
			obj.put(NORMAL_KEY,  entry.getKey());
			obj.put(NORMAL_VALUE, entry.getValue());
			array.add(obj);
		}
		return array;
	}
	
}
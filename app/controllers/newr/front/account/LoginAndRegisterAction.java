package controllers.newr.front.account;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.json.JSONObject;

import org.apache.commons.lang.StringUtils;

import play.Logger;
import play.cache.Cache;
import play.libs.Codec;
import play.libs.Images;
import play.mvc.Scope;
import play.mvc.Scope.Session;
import play.mvc.With;
import utils.CaptchaUtil;
import utils.ErrorInfo;
import utils.JSONUtils;
import utils.MobileUtil;
import utils.RegexUtils;
import utils.SMSUtil;
import utils.Security;
import business.BottomLinks;
import business.DealDetail;
import business.newr.User;

import com.shove.security.Encrypt;

import constants.UserEvent;
import constants.newr.Constants;
import controllers.newr.BaseController;
import controllers.newr.DSecurity;

/**
 * 
 * @author liuwenhui
 * 
 */
@With(DSecurity.class)
public class LoginAndRegisterAction extends BaseController {

	/**
	 * 跳转到登录页面
	 */
	public static void login(String fromUrl) {
		Scope.Params p = params;
		String encryString = Session.current().getId();
		Integer  loginCount= (Integer)Cache.get("loginCount" + encryString);
		flash.put("loginCount", loginCount == null ? 0 : loginCount);
 		render();
	}

	public static void logout() {
		User user = User.currUser();

		if (user == null) {
			LoginAndRegisterAction.login(null);
		}

		ErrorInfo error = new ErrorInfo();

		user.logout(error);

		if (error.code < 0) {
			render(Constants.ERROR_PAGE_PATH_FRONT);
		}

		login(null);
	}

	/**
	 * 包含登录页面的登录
	 */
	public static void logining() {
		String encryString = Session.current().getId();
		Integer loginCount = (Integer )Cache.get("loginCount" + encryString);
		loginCount = loginCount == null ? 0 : loginCount;
		Cache.set("loginCount"+encryString, ++loginCount, Constants.CACHE_TIME_MINUS_30);
		
		business.BackstageSet  currBackstageSet = business.BackstageSet.getCurrentBackstageSet();
		Map<String,java.util.List<business.BottomLinks>> bottomLinks = business.BottomLinks.currentBottomlinks();
		   
		if(null != currBackstageSet){
		  Cache.delete("backstageSet");//清除系统设置缓存
		}
		   
		if(null != bottomLinks){
		  Cache.delete("bottomlinks");//清除底部连接缓存
		}
	   
		ErrorInfo error = new ErrorInfo();

		String name = params.get("name");
		String url = request.headers.get("referer").value();
		
		StringBuffer url2 = new StringBuffer();
		String password = params.get("password");
		String code = params.get("code");
		String randomID = params.get("randomID");
		flash.put("name", name);
		flash.put("code", code);
		
		if (StringUtils.isBlank(name)) {
			flash.error("请输入用户名");
			redirect(url);
		}

		if (StringUtils.isBlank(password)) {
			flash.error("请输入密码");
			redirect(url);
		}

		if (loginCount > 3 && StringUtils.isBlank(code)) {
			flash.error("请输入验证码");
			redirect(url);

		}

		if (loginCount > 3 && StringUtils.isBlank(randomID)) {
			flash.error("请刷新验证码");
			redirect(url);
		}

		if (Constants.CHECK_CODE) {
			
			if(loginCount > 3 && !code.equalsIgnoreCase(CaptchaUtil.getCode(randomID))) {
				flash.error("验证码错误");
				redirect(url);
			}
		}
		
		User user = new User();

		name = StringUtils.trimToEmpty(name);
		if(RegexUtils.isEmail(name)){
			//todo user.findUserByEmail(name);
		}else if(RegexUtils.isMobileNum(name)){
			// todo user.findUserByMobile(name);
		}else{
			user.name = name;
		}        
		if (user.id < 0) {
			flash.error("密码或用户名有误!"); //flash.error("该用户名不存在");
			redirect(url);
		}
			
		if (user.login(password,false, Constants.CLIENT_PC, error) < 0) {
			//add 20160612记录用户操作失败记录 
			flash.error(error.msg);
			DealDetail.userEvent(user.id, UserEvent.LOGIN, "登录失败", error);
			redirect(url);
		}
		Cache.delete("loginCount"+encryString);		
		AccountHome.home();

		
	}

	/**
	 * 登录页面登录logining
	 */
	public static void topLogin() {
		ErrorInfo error = new ErrorInfo();

		String userName = params.get("name");
		String password = params.get("password");
		String code = params.get("code");
		String randomID = params.get("randomID");

		flash.put("name", userName);
		flash.put("code", code);

		if (StringUtils.isBlank(userName)) {
			flash.error("请输入用户名");
			login(null);
		}

		if (StringUtils.isBlank(password)) {
			flash.error("请输入密码");
			login(null);
		}

		if (StringUtils.isBlank(code)) {
			flash.error("请输入验证码");
			login(null);

		}

		if (StringUtils.isBlank(randomID)) {
			flash.error("请刷新验证码");
			login(null);
		}

		if (!code.equalsIgnoreCase(CaptchaUtil.getCode(randomID))) {
			flash.error("验证码错误");
			login(null);
		}

		User user = new User();
		user.name = userName;

		if (user.login(password,false, Constants.CLIENT_PC, error) < 0) {
			flash.error(error.msg);
			login(null);
		}

		AccountHome.home();
	}

	/**
	 * 邮箱注册页面
     *un 推荐用户名称加密串
	 */
	public static void register(String un, String recommendUserName) {
		String loginOrRegister = Constants.LOGIN_AREAL_FLAG;

		ErrorInfo error = new ErrorInfo();
		String name = "";
		
		if(!StringUtils.isBlank(un)){
			name = Encrypt.decrypt3DES(un, Constants.ENCRYPTION_KEY);
			
		}
		
		if(!StringUtils.isBlank(recommendUserName)){
			name = recommendUserName;
			
		}
		
		flash.put("recommendUserName", name);

	}
	
	/**
	 * 手机注册页面
	 */
	public static void registerMobile(String nameForRecommend) {
		String loginOrRegister = Constants.LOGIN_AREAL_FLAG;
		
		ErrorInfo error = new ErrorInfo();
		flash.put("recommendUserName", nameForRecommend);
		
	}
	
	/**
	 * 获取验证码并返回页面
	 */
	public static void codeReturn(String codeImg) {
		String randomID = (String) Cache.get(codeImg);

		JSONObject json = new JSONObject();
		json.put("randomID", randomID);

		renderJSON(json);
	}

	/**
	 * 验证注册
	 * 
	 * @param t_users
	 */
	public static void registering() {
		checkAuthenticity();
		ErrorInfo error = new ErrorInfo();

		String name = params.get("userName");
		String email = params.get("email");
		
		if (null != email) {
			email = email.toLowerCase();
		}
		
		String mobile = params.get("mobile");
		String smsCode = params.get("smsCode");
		String password = params.get("password");
		String randomID = (String) Cache.get(params.get("randomID"));
		String code = params.get("code");
		String recommendUserName = params.get("recommended");
		String recommendName = params.get("recommendName");
		String recommendMobile = params.get("recommendId");
		flash.put("userName", name);
		flash.put("email", email);
		flash.put("mobile", mobile);
		flash.put("smsCode", smsCode);
		flash.put("recommendUserName", recommendUserName);
		flash.put("code", code);
		
		String NameForReccommend = "";
		
		if(!StringUtils.isBlank(recommendUserName)){
			NameForReccommend = recommendUserName;
		}
		
		if(!StringUtils.isBlank(recommendName)){
			NameForReccommend = recommendName;
		}
		
		boolean isMobileRegister = StringUtils.isNotBlank(mobile) ? true : false;
		
		if (StringUtils.isBlank(name)) {
			flash.error("请填写用户名");
			
			if (isMobileRegister) {
				registerMobile(NameForReccommend);
			} else {
				register("","");
			}
		}

		if (StringUtils.isBlank(password)) {
			flash.error("请输入密码");

			if (isMobileRegister) {
				registerMobile(NameForReccommend);
			} else {
				register("","");
			}
		}

		if (StringUtils.isBlank(code)) {
			flash.error("请输入验证码");

			if (isMobileRegister) {
				registerMobile(NameForReccommend);
			} else {
				register("","");
			}
		}

		if (!RegexUtils.isValidUsername(name)) {
			flash.error("请填写符合要求的用户名");

			if (isMobileRegister) {
				registerMobile(NameForReccommend);
			} else {
				register("","");
			}
		}

		if (isMobileRegister) {
			if (!RegexUtils.isMobileNum(mobile)) {
				flash.error("请填写正确的手机号码");
				registerMobile(NameForReccommend);
			}
			
			if (StringUtils.isBlank(smsCode)) {
				flash.error("请输入短信校验码");
				registerMobile(NameForReccommend);
			}
			
			if(Constants.CHECK_MSG_CODE) {
				String cCode = (String) Cache.get(mobile);
				
				if(cCode == null) {
					flash.error("短信校验码已失效，请重新点击发送校验码");
					registerMobile(NameForReccommend);
				}
				
				if(!smsCode.equals(cCode)) {
					flash.error("短信校验码错误");
					registerMobile(NameForReccommend);
				}
			}
			User.isMobileExist(mobile, null, error);

			if (error.code < 0) {
				flash.error(error.msg);
				
				registerMobile(NameForReccommend);
			}
		}
		
		
		if (!RegexUtils.isValidPassword(password)) {
			flash.error("请填写符合要求的密码");

			if (isMobileRegister) {
				registerMobile(NameForReccommend);
			} else {
				register("","");
			}
		}

		if(Constants.CHECK_CODE && !isMobileRegister){
			
			if(!code.equalsIgnoreCase(randomID)) {
				flash.error("图片校验码错误");
				register("","");
			}
		}
		
		User.isNameExist(name, error);

		if (error.code < 0) {
			flash.error(error.msg);
			register("","");
		}
		
		String recoName = "";
		
		if(StringUtils.isNotBlank(NameForReccommend)) {
			User.isNameExist(NameForReccommend, error);
			if (error.code == -2) {
				recoName = NameForReccommend;
		    }
		}
		
		if (isMobileRegister) {
			User.isMobileExist(email, null, error);
			
			if (error.code < 0) {
				flash.error(error.msg);
				registerMobile(NameForReccommend);
			}
		}

		
		User user = new User();
		user.time = new Date();
		
		user.name = name;
		user.password = password;
		
		
		user.register(Constants.CLIENT_PC, error);

		if (error.code < 0) {
			flash.error(error.msg);
			register("","");
		}

		AccountHome.home();
	}

	/**
	 * 发送短信验证码
	 * @param mobile
	 */
	public static void sendSmsCode(String mobile,boolean send) {
		Logger.debug("\n类：%s\n方法：%s\n参数：%s", Thread.currentThread().getStackTrace()[1].getClassName(), Thread.currentThread().getStackTrace()[1].getMethodName(), JSONObject.fromObject(params.data));
		ErrorInfo error = new ErrorInfo();
		
		if(StringUtils.isBlank(mobile) ) {
			error.code = -1;
			error.msg = "手机号码不能为空";
			
			renderJSON(error);
		}
		
		if(!RegexUtils.isMobileNum(mobile)) {
			error.code = -1;
			error.msg = "手机号格式有误";
			
			renderJSON(error);
		}
		
		if(User.isMobileExist(mobile, null, error) < 0){
			renderJSON(error);
		}
		if(send){
			SMSUtil.sendCode(mobile, error);
		}
		
		renderJSON(error);
	}

	/**
	 * 生成验证码图片
	 * 
	 * @param id
	 */
	public static void getImg(String id) {
		Images.Captcha captcha = CaptchaUtil.CaptchaImage(id);
		if(captcha == null){
			return;
		}

		renderBinary(captcha);
	}

	/**
	 * 刷新验证码
	 */
	public static void setCode() {
		String randomID = CaptchaUtil.setCaptcha();

		JSONObject json = new JSONObject();
		json.put("img", randomID);
		renderJSON(json.toString());
	}

	/**
	 * 校验验证码
	 */
	public static void checkCode(String randomId, String code) {

		ErrorInfo error = new ErrorInfo();
		JSONObject json = new JSONObject();

		if (StringUtils.isBlank(code)) {
			error.code = -1;
			error.msg = "请输入验证码";

			json.put("error", error);
			renderJSON(json);
		}

		if (StringUtils.isBlank(randomId)) {
			error.code = -1;
			error.msg = "请刷新验证码";

			json.put("error", error);
			renderJSON(json);
		}

		String radomCode = CaptchaUtil.getCode(randomId);

		if (!code.equalsIgnoreCase(radomCode)) {
			error.code = -1;
			error.msg = "验证码错误";

			json.put("error", error);
			renderJSON(json);
		}

		json.put("error", error);
		renderJSON(json);
	}

	

	/**
	 * 验证用户名是否已存在
	 * 
	 * @param name
	 */
	public static void hasNameExist(String name) {
		ErrorInfo error = new ErrorInfo();
		User.isNameExist(name, error);
		
		JSONObject json = new JSONObject();
		json.put("error", error);
		renderJSON(json.toString());  //code>0 用户名不存在。code<0用户名已存在或验证异常
	}
	

	/**
	 * 验证手机号码是否已存在
	 * 
	 * @param name
	 */
	public static void hasMobileExist(String telephone) {
		ErrorInfo error = new ErrorInfo();

		int nameIsExist = User.isMobileExist(telephone, null, error);

		JSONObject json = new JSONObject();
		json.put("result", nameIsExist);

		renderJSON(json.toString());
	}

	/**
	 * 底部链接信息查询
	 * 
	 * @param name
	 */
	public static void buttomLinks(String num) {
		/* 初始化底部链接 */
		List<BottomLinks> result = BottomLinks.queryFrontBottomLinks(num);
		JSONObject json = new JSONObject();
		json.put("result", result);
		renderJSON(json.toString());
	}
	/**
	 * 注册跳转到成功页面
	 */
	public static void registerSuccess() {
		User user = User.currUser();
		if (user == null) {
			login(null);
		}
	
			AccountHome.home();	

	}


	/**
	 * 通过手机找回用户名
	 */
	public static void findBackUsernameByTele() {
		String loginOrRegister = Constants.LOGIN_AREAL_FLAG;
		render(loginOrRegister);
	}

	/**
	 * 通过手机找回用户名后跳转到成功页面
	 */
	public static void teleSuccess() {
		String loginOrRegister = Constants.LOGIN_AREAL_FLAG;
		render(loginOrRegister);
	}

	/**
	 * 发送找回用户的短信
	 */
	public static void saveUsernameByTele(String mobile, String code,
			String randomID) {
		ErrorInfo error = new ErrorInfo();

		flash.put("mobile", mobile);
		flash.put("code", code);

		if (StringUtils.isBlank(mobile)) {
			flash.error("请输入手机号码");
			findBackUsernameByTele();
		}

		if (StringUtils.isBlank(code)) {
			flash.error("请输入验证码");
			findBackUsernameByTele();
		}

		if(Cache.get(randomID) == null) {
			flash.error("请刷新验证码");
			findBackUsernameByTele();
		}

		if (StringUtils.isBlank(randomID)) {
			flash.error("请刷新验证码");
			findBackUsernameByTele();
		}

		if (!RegexUtils.isMobileNum(mobile)) {
			flash.error("请输入正确的手机号码");
			findBackUsernameByTele();
		}

		if (!code.equalsIgnoreCase(CaptchaUtil.getCode(randomID))) {
			flash.error("验证码错误");
			findBackUsernameByTele();
		}

		User.isMobileExist(mobile, null, error);

		if (error.code != -2) {
			flash.error("该手机号码不存在或未绑定");
			findBackUsernameByTele();
		}

		MobileUtil.mobileFindUserName(mobile, error);

		if (error.code < 0) {
			flash.error(error.msg);
			findBackUsernameByTele();
		}
		
		if (error.code < 0) {
			flash.error(error.msg);
			findBackUsernameByTele();
		}

		flash.put("code", "");
		flash.error(error.msg);

		login(null);
	}

	/**
	 * 通过手机重置密码
	 */
	public static void resetPasswordByMobile() {
		String loginOrRegister = Constants.LOGIN_AREAL_FLAG;
		String randomId = Codec.UUID();
		boolean checkMsgCode = Constants.CHECK_PIC_CODE;
		
		render(loginOrRegister, randomId, checkMsgCode);
	}

	/**
	 * 保存重设的密码
	 */
	public static void savePasswordByMobile(String mobile, String code,
			String password, String confirmPassword, String randomID, String captcha,String idNumber) {
		ErrorInfo error = new ErrorInfo();
        
		User.updatePasswordByMobileAndIdNumber(mobile, code, password, confirmPassword,
				randomID, captcha, idNumber,error);

		if (error.code < 0) {			
			flash.put("mobile", mobile);
			flash.put("code", code);
			flash.put("captcha", captcha);
			
			flash.error(error.msg);
			
			resetPasswordByMobile();
		}

		flash.error(error.msg);

		login(null);
	}

	
	/**
	 * 跳转到重置密码页面
	 */
	public static void resetPassword(String sign) {
		String loginOrRegister = Constants.LOGIN_AREAL_FLAG;
		ErrorInfo error = new ErrorInfo();
		long id = Security.checkSign(sign, Constants.PASSWORD, Constants.VALID_TIME, error);
		
		if(error.code < 0) {
			flash.error(error.msg);
			login(null);
		}
		
		String name = User.queryUserNameById(id, error);
		
		render(loginOrRegister, name,sign);
	}
	
	/**
	 * 保存重置密码
	 */
	public static void savePasswordByEmail(String sign, String password, String confirmPassword) {
		ErrorInfo error = new ErrorInfo();
		
		long id = Security.checkSign(sign, Constants.PASSWORD, Constants.VALID_TIME, error);
		
		if(error.code < 0) {
			flash.error(error.msg);
			login(null);
		}
		
		User user = new User();
		user.id = id;
		user.updatePasswordByEmail(password, confirmPassword, error);
		
		if(error.code < 0) {
			flash.error(error.msg);
			resetPassword(sign);
		}
		
		flash.error(error.msg);
		
		login(null);
	}
	
	/**
	 * 发送手机校验码
	 * @param code
	 */
    public static void verifyMobile(String mobile, String captcha, String randomID) {
        ErrorInfo error = new ErrorInfo();
        JSONObject json = new JSONObject();
        
        if (StringUtils.isBlank(mobile)) {
            error.code = -1;
            error.msg = "请输入手机号码";

            json.put("error", error);

            renderJSON(json);
        }

        if (!RegexUtils.isMobileNum(mobile)) {
            error.code = -1;
            error.msg = "请输入正确的手机号码";

            json.put("error", error);

            renderJSON(json);
        }
        
        if (Constants.CHECK_PIC_CODE) {
			if (StringUtils.isBlank(captcha)) {
				
				error.code = -1;
				error.msg = "请输入图形验证码";
				
				json.put("error", error);
				
				renderJSON(json);
			}
			
			if (StringUtils.isBlank(randomID)) {
				
	        	error.code = -1;
	        	error.msg = "请刷新图形验证码";
	        	
	        	json.put("error", error);
	        	
	        	renderJSON(json);
			}
	        
	        String codec = (String) Cache.get(randomID);
	        if (!codec.equalsIgnoreCase(captcha)) {
				
	        	error.code = -1;
	        	error.msg = "图形验证码错误";
	        	
	        	json.put("error", error);
	        	
	        	renderJSON(json);
			}
  		}
        
    	/**
    	 * 设置手机号码60S内无法重复发送验证短信
    	 */        
        if(Constants.CHECK_MSG_CODE){
        	Object cache = Cache.get("mobile");
        	if(null == cache){
        		Cache.set("mobile", mobile,Constants.CACHE_TIME_SECOND_60);
        	}else{
        		String isOldMobile = cache.toString();
        		if (isOldMobile.equals(mobile)) {
        			error.code = -1;
        			error.msg = "短信已发送";
        			renderJSON(error);
        		}
        	}
        }

        User user = User.currUser();

        if (user == null || StringUtils.isBlank(user.mobile) || !user.mobile.equals(mobile)) {
            User.isMobileExist(mobile, null, error);

            if (error.code != 0) {

                json.put("error", error);

                renderJSON(json);
            }
        }

        SMSUtil.sendCode(mobile, error);

        json.put("error", error);

        renderJSON(json);
    }
    
    
    /**
     * <Description functions in a word>
     * 手机密码找回
     * <Detail description>
     * @author  ChenZhipeng
     * @param mobile
     * @param captcha
     * @param randomID [Parameters description]
     * @return void [Return type description]
     * @exception throws [Exception] [Exception description]
     * @see [Related classes#Related methods#Related properties]
     */
    public static void findPasswordByMobile(String mobile, String captcha, String randomID) {
        ErrorInfo error = new ErrorInfo();
        JSONObject json = new JSONObject();
        
        if (StringUtils.isBlank(mobile)) {
            error.code = -1;
            error.msg = "请输入手机号码";

            json.put("error", error);

            renderJSON(json);
        }

        if (!RegexUtils.isMobileNum(mobile)) {
            error.code = -1;
            error.msg = "请输入正确的手机号码";

            json.put("error", error);

            renderJSON(json);
        }
         
    	/**
    	 * 设置手机号码60S内无法重复发送验证短信
    	 */        
        if(Constants.CHECK_MSG_CODE){
        	Object cache = Cache.get("mobile");
        	if(null == cache){
        		Cache.set("mobile", mobile,Constants.CACHE_TIME_MINUS_2);
        	}else{
        		String isOldMobile = cache.toString();
        		if (isOldMobile.equals(mobile)) {
        			error.code = -1;
        			error.msg = "短信已发送";
        			json.put("error", error);
        			renderJSON(json);
        		}
        	}
        }

        User user = User.currUser();

        if (user == null || StringUtils.isBlank(user.mobile) || !user.mobile.equals(mobile)) {
            User.isMobileExist(mobile, null, error);

            if (error.code != -2) {
            	error.code = -1;
            	error.msg= "手机号码不存在";
            	
                json.put("error", error);

                renderJSON(json);
            }
        }

        SMSUtil.sendCode(mobile, error);
        
        json.put("error", error);

        renderJSON(json);
    }
    
    /**
	 * APP手机号码注册页面
	 */
	public static void appReg(String recommendName) {
		render(recommendName);
	}
	
	/**
	 * APP手机号码注册
	 * @param parameters
	 * @return
	 * @throws IOException 
	 */
	public static String appRegMobile(String name,String mobile,String smsCode,String password,String recommendName) throws IOException {
		ErrorInfo error = new ErrorInfo();

		
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("code", "-2");
		
		if (StringUtils.isBlank(name)) {
			map.put("msg", "请填写用户名");
			return JSONUtils.printObject(map);
		}

		if (StringUtils.isBlank(password)) {
			map.put("msg", "请输入密码");
			return JSONUtils.printObject(map);
		}

		if (!RegexUtils.isValidUsername(name)) {
			map.put("msg", "请填写符合要求的用户名");
			return JSONUtils.printObject(map);
		}

		if (!RegexUtils.isMobileNum(mobile)) {
			map.put("msg", "请填写正确的手机号码");
			return JSONUtils.printObject(map);
		}
		
		if (StringUtils.isBlank(smsCode)) {
			map.put("msg", "请输入短信校验码");
			return JSONUtils.printObject(map);
		}
		
		if(Constants.CHECK_MSG_CODE) {
			String cCode = (String) Cache.get(mobile);
			
			if(cCode == null) {
				map.put("msg", "校验码已失效，请重新点击发送校验码");
				return JSONUtils.printObject(map);
			}
			
			if(!smsCode.equals(cCode)) {
				map.put("msg", "校验码错误");
				return JSONUtils.printObject(map);
			}
		}
		
		User.isMobileExist(mobile, null, error);

		if (error.code < 0) {
			map.put("msg", error.msg);
			return JSONUtils.printObject(map);
		}
		
		if (!RegexUtils.isValidPassword(password)) {
			map.put("msg", "请填写符合要求的密码");
			return JSONUtils.printObject(map);
		}
		
		User.isMobileExist(mobile, null, error);

		if (error.code < 0) {
			map.put("msg", error.msg);
			return JSONUtils.printObject(map);
		}
		
		User.isNameExist(name, error);

		if (error.code < 0) {
			map.put("msg", error.msg);
			return JSONUtils.printObject(map);
		}

		User user = new User();
		user.time = new Date();
		user.name = name;
		user.password = password;
		user.mobile = mobile;
		user.register(Constants.CLIENT_PC, error);
		
		if (error.code < 0) {
			map.put("msg", error.msg);
			return JSONUtils.printObject(map);
		}

		map.put("msg", "注册成功");
		map.put("code", "0");
		return JSONUtils.printObject(map);
	}
	
	/**
	 * 发送手机校验码
	 * @param code
	 */
    public static void verifyMobileForReg(String mobile, String code, String randomID) {
        ErrorInfo error = new ErrorInfo();
        
        if (StringUtils.isBlank(mobile)) {
            error.code = -1;
            error.msg = "请输入手机号码";
            renderJSON(error);
        }

        if (!RegexUtils.isMobileNum(mobile)) {
            error.code = -1;
            error.msg = "请输入正确的手机号码";
            renderJSON(error);
        }
        
        if (Constants.CHECK_PIC_CODE) {
			if (StringUtils.isBlank(code)) {
				error.code = -1;
				error.msg = "请输入图形验证码";
				renderJSON(error);
			}
			
			if (StringUtils.isBlank(randomID)) {
	        	error.code = -1;
	        	error.msg = "请刷新图形验证码";
	        	renderJSON(error);
			}
	        
	        String codec = (String) Cache.get(randomID);
	        if (!codec.equalsIgnoreCase(code)) {
	        	error.code = -1;
	        	error.msg = "图形验证码错误";
	        	renderJSON(error);
			}
  		}
    	
    	/**
    	 * 设置手机号码60S内无法重复发送验证短信
    	 */        
        if(Constants.CHECK_MSG_CODE){
        	Object cache = Cache.get("mobile");
        	if(null == cache){
        		Cache.set("mobile", mobile,Constants.CACHE_TIME_SECOND_60);
        	}else{
        		String isOldMobile = cache.toString();
        		if (isOldMobile.equals(mobile)) {
        			error.code = -1;
        			error.msg = "短信已发送";
        			renderJSON(error);
        		}
        	}
        }
        
        if(User.isMobileExist(mobile, null, error) < 0){
			renderJSON(error);
		}
        SMSUtil.sendCode(mobile, error);
        renderJSON(error);
    }
    
	/**
	 * 检验邮件注册图形验证码
	 * @param code
	 * @param randomID
	 */
    public static void verifyEmailForReg(String code, String randomID) {
    	ErrorInfo error = new ErrorInfo();
    	
    	if (Constants.CHECK_PIC_CODE) {
			if (StringUtils.isBlank(code)) {
				error.code = -1;
				error.msg = "请输入图形验证码";
				renderJSON(error);
			}
			
			if (StringUtils.isBlank(randomID)) {
	        	error.code = -1;
	        	error.msg = "请刷新图形验证码";
	        	renderJSON(error);
			}
	        
	        String codec = (String) Cache.get(randomID);
	        if (!codec.equalsIgnoreCase(code)) {
	        	error.code = -1;
	        	error.msg = "图形验证码错误";
	        	renderJSON(error);
			}
  		}
    	renderJSON(error);
    }
    

    /***
     * <Description functions in a word>
     * 短信校验码检验
     * <Detail description>
     * @author  ChenZhipeng
     * @param mobile 手机号码
     * @param smsCode 短信校验码
     * @return void [Return type description]
     * @exception throws [Exception] [Exception description]
     * @see [Related classes#Related methods#Related properties]
     */
    public static void verifySmsCodeForReg(String mobile, String smsCode) {
    	ErrorInfo error = new ErrorInfo();
    	
    	if (Constants.CHECK_MSG_CODE) {
	        String codec = (String) Cache.get(mobile);
	        if (!smsCode.equalsIgnoreCase(codec)) {
	        	error.code = -1;
	        	error.msg = "短信校验码输入错误";
	        	renderJSON(error);
			}
  		}
    	renderJSON(error);
    }
    
    
    /**
	 * 验证手机号码是否已存在
	 * 
	 * @param mobile
	 */
	public static void hasMobileExists(String mobile) {
		ErrorInfo error = new ErrorInfo();
		if (StringUtils.isBlank(mobile)) {
            error.code = -1;
            error.msg = "请输入手机号码";
            renderJSON(error);
        }

        if (!RegexUtils.isMobileNum(mobile)) {
            error.code = -1;
            error.msg = "请输入正确的手机号码";
            renderJSON(error);
        }
		User.isMobileExist(mobile, null, error);
		renderJSON(error);
	}
	
    /**
	 * 验证用户名和密码是否正确
	 * 
	 * @param mobile
	 */
	public static void verifyLogin(String name, String password){
//		User user = User.currUser();
		ErrorInfo error = new ErrorInfo();
		User user = new User();

		name = StringUtils.trimToEmpty(name);
		if(RegexUtils.isMobileNum(name)){
			// todo user.findUserByMobile(name);
		}else{
			user.name = name;
		}
        
		if (user.id < 0) {
			error.code = -1;
			error.msg = "该用户名不存在";
			renderJSON(error);
		}else if (user.login(password,false, Constants.CLIENT_PC, error) < 0) {
			renderJSON(error);
		}else{
			error.code = 0;
			error.msg = "登录成功";
			renderJSON(error);
		}
		
	}
}

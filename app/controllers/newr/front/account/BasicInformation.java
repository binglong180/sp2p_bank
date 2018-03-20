package controllers.newr.front.account;

import net.sf.json.JSONObject;
import play.mvc.With;
import utils.ErrorInfo;
import business.newr.User;
import controllers.interceptor.newr.FInterceptor;
import controllers.newr.BaseController;

@With({FInterceptor.class})
public class BasicInformation extends BaseController {
	/**
	 * 修改密码
	 */
	public static void modifyPassword(){
		User user = User.currUser();
		render(user);
	}
	
	/**
	 * 保存密码
	 * @param oldPassword
	 * @param newPassword1
	 * @param newPassword2
	 */

	public static void savePassword(String oldPassword, String newPassword1, 
			String newPassword2){
		User user = User.currUser();
		ErrorInfo error = new ErrorInfo();
		
		if(oldPassword.equalsIgnoreCase(newPassword1)){
			JSONObject json = new JSONObject();
			json.put("error", "新密码与原密码一样，请重新输入");
			renderJSON(json);
		}
		
		user.editPassword(oldPassword, newPassword1, newPassword2, error);
		
		JSONObject json = new JSONObject();
		json.put("error", error);
		renderJSON(json);
	}
	
	
	
}

package controllers.newr.front;

import utils.ErrorInfo;
import business.newr.User;
import constants.Constants;
import controllers.newr.BaseController;
/**
 * 
 * @author liuwenhui
 *
 */

public class NewHomeAction extends BaseController {
	/* 网站首页 */
	public static void home() {
		int currPage = Constants.ONE;
		int pageSize = Constants.THREE;
		ErrorInfo error = new ErrorInfo();
		User user = new User();
		User tempUser = User.currUser();
/*		PageBean<v_front_all_bids>  pageBean = new PageBean<v_front_all_bids>();
		pageBean= Invest.queryAllBids(Constants.SHOW_TYPE_1,currPage,pageSize,error);
		
		PageBean<News> notice = News.queryNewsBySupervisor("1", "7", "", "0", "", "1", "5", error); //2016 add chencheng //平台广告
		PageBean<News> media = News.queryNewsBySupervisor("1", "8", "", "0", "", "1", "5", error); //2016 add chencheng //媒体报道
		if(tempUser!=null){
			user.id = User.currUser().id;
			List<t_content_advertisements> homeAds = Ads.queryAdsByLocation(Constants.HOME_PAGE_PC, error); // 广告条
			render(homeAds,user,pageBean,notice,media);	
		}else{
			List<t_content_advertisements> homeAds = Ads.queryAdsByLocation(Constants.HOME_PAGE_PC, error); // 广告条
			render(homeAds,pageBean,notice,media);
		}*/
		
	}
	// 新手指引
	public static void newer() {
		System.out.println("------------------------------------------wwwwww");
	  render();
		
	}
	// 关于我们
	public static void aboutUs() {
	  render();
		
	}
	
	// 关于账户
	public static void helpCenterAccount() {
	  render();
		
	}
	// 关于投资
	public static void helpCenterInvest() {
	  render();
		
	}
	// 常见问题
	public static void helpCenterProblems() {
	  render();
		
	}
    // 名词解释
	public static void helpCenterTerms() {
	  render();
		
	}
	
	// 合作机构
	public static void cooperation() {
	  render();
		
	}
	
	// 新版上线公告
	public static void newOnline() {
		  render();
	}
	
	// 管理团队
	public static void manageTeam() {
		render();
				
	}
	
	// 办公环境
	public static void workEnv() {
		render();		
	}
	
	// 首页导航
	public static void newr() {
		render();		
	}
	
	// 我要借款
	public static void bid() {
		render();		
	}
}
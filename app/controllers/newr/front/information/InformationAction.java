package controllers.newr.front.information;

import java.util.ArrayList;
import java.util.List;

import models.t_content_news_types;

import org.apache.commons.lang.StringUtils;

import utils.ErrorInfo;
import utils.NumberUtil;
import utils.PageBean;
import business.News;
import business.NewsType;
import constants.Constants;
import controllers.newr.BaseController;
/**
 * 
 * @author liuwenhui
 *
 */

public class InformationAction extends BaseController {

	// 媒体列表
	public static void mediaList() {
		String topTypes = "1";
		String typeIdStr = "8";
		String title = "";
		String orderType = "0";
		String orderStatus = "";
		
		
		String currPage  = params.get("currPage");
		String pageSize = params.get("pageSize");
		ErrorInfo error = new ErrorInfo();

		PageBean<News> page = News.queryNewsBySupervisor(topTypes, typeIdStr, title,
				orderType, orderStatus, currPage, pageSize, error);

		if(error.code < 0){
			render(Constants.ERROR_PAGE_PATH_SUPERVISOR);
		}
		
		List <NewsType> types=  NewsType.queryTopTypes( error);
		
		if(error.code < 0){
			render(Constants.ERROR_PAGE_PATH_SUPERVISOR);
		}
		
		List<NewsType> childTypes = new ArrayList<NewsType>();
		
		t_content_news_types parentType = null;
		
		if(StringUtils.isNotBlank(typeIdStr)){
			if(NumberUtil.isNumericInt(typeIdStr)) {
				NewsType type = new NewsType();
				type.id = Long.parseLong(typeIdStr);
				parentType = NewsType.queryParentType(type.parentId, error);
				
				if(error.code < 0) {
					render(Constants.ERROR_PAGE_PATH_SUPERVISOR);
				}
				
				childTypes = NewsType.queryChildTypes(parentType.id, error);
				
				if(error.code < 0) {
					render(Constants.ERROR_PAGE_PATH_SUPERVISOR);
				}
	 		}
		}else if(StringUtils.isNotBlank(topTypes)) {
 			
			if(NumberUtil.isNumericInt(topTypes)) {
				childTypes = NewsType.queryChildTypes(Long.parseLong(topTypes), error);
 	 		}
 			
			if(error.code < 0) {
				render(Constants.ERROR_PAGE_PATH_SUPERVISOR);
			}
 	 	}
		
		
		render(page,types, childTypes, parentType);
		
	}
	// 媒体详情
	public static void mediaDetail(String id) {
		ErrorInfo error = new ErrorInfo();
	
		List <News> newsDetail = News.queryNewsDetail(id, null, error);
		
		if(error.code < 0) {
			render(Constants.ERROR_PAGE_PATH_FRONT);
		}
		
		render(newsDetail);
	}
	
	// 公告列表
	public static void noticeList() {
		String topTypes = "1";
		String typeIdStr = "7";
		String title = "";
		String orderType = "0";
		String orderStatus = "";
		
		
		String currPage  = params.get("currPage");
		String pageSize = params.get("pageSize");
		ErrorInfo error = new ErrorInfo();

		PageBean<News> page = News.queryNewsBySupervisor(topTypes, typeIdStr, title,
				orderType, orderStatus, currPage, pageSize, error);

		if(error.code < 0){
			render(Constants.ERROR_PAGE_PATH_SUPERVISOR);
		}
		
		List <NewsType> types=  NewsType.queryTopTypes( error);
		
		if(error.code < 0){
			render(Constants.ERROR_PAGE_PATH_SUPERVISOR);
		}
		
		List<NewsType> childTypes = new ArrayList<NewsType>();
		
		t_content_news_types parentType = null;
		
		if(StringUtils.isNotBlank(typeIdStr)){
			if(NumberUtil.isNumericInt(typeIdStr)) {
				NewsType type = new NewsType();
				type.id = Long.parseLong(typeIdStr);
				parentType = NewsType.queryParentType(type.parentId, error);
				
				if(error.code < 0) {
					render(Constants.ERROR_PAGE_PATH_SUPERVISOR);
				}
				
				childTypes = NewsType.queryChildTypes(parentType.id, error);
				
				if(error.code < 0) {
					render(Constants.ERROR_PAGE_PATH_SUPERVISOR);
				}
	 		}
		}else if(StringUtils.isNotBlank(topTypes)) {
 			
			if(NumberUtil.isNumericInt(topTypes)) {
				childTypes = NewsType.queryChildTypes(Long.parseLong(topTypes), error);
 	 		}
 			
			if(error.code < 0) {
				render(Constants.ERROR_PAGE_PATH_SUPERVISOR);
			}
 	 	}
		
		
		render(page,types, childTypes, parentType);
		
	}
	// 公告详情
	public static void noticeDetail(String id) {

		ErrorInfo error = new ErrorInfo();
    	
    	List <News> newsDetail = News.queryNewsDetail(id, null, error);
    	
    	if(error.code < 0) {
    		render(Constants.ERROR_PAGE_PATH_FRONT);
    	}
    	
    	render(newsDetail);
		
	}
	
	// 联系我们
	public static void contactUs() {
	  render();
	}
	
	// 联系我们
	public static void security() {
	  render();
	}

}
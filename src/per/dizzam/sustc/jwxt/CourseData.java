﻿package per.dizzam.sustc.jwxt;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.util.Map.Entry;

import org.apache.http.ParseException;
import org.apache.http.auth.AuthenticationException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.internal.Streams;
import com.google.gson.stream.JsonWriter;

import per.dizzam.sustc.cas.NetworkConnection;
import per.dizzam.sustc.cas.Method;

public class CourseData extends NetworkConnection {
	
	private Logger logger = Logger.getLogger("CourseCenter");
	
	JsonObject course;
	JsonArray selected;
	
	private final String coursestorge = "course.json";
	private final String selectedstorge = "selected.json";
	
//	private final String Xsxk = "/xsxk/xsxk_index?jx0502zbid=054B5FA7E55F44E0BB3D24DB3BC561D5";	//主页，用于打开主页权限
//	private final String Xsxk = "/xsxk/xsxk_index?jx0502zbid=054B5FA7E55F44E0BB3D24DB3BC561";	//主页，用于打开主页权限
	private final String Xsxk = "/xsxk/xsxk_index?jx0502zbid=2A018810EA3C4DCFAD5A971752AD1A0D";	//主页，用于打开主页权限
//	private final String Xkjglb = "/xsxkjg/xsxkBxqjhxk";	//已选课程----I non't know why I write this, but I'll keep it before I confirm it is unused.
	private final String Xkjglb = "/xsxkjg/comeXkjglb";	//已选课程
	
	private final String query = "/xsxkkc/xsxk%s?kcxx=&skls=&skxq=&skjc=&sfym=false&sfct=false";
	
	public static enum CourseRepo {
		Bxxk("必修选课"),
		Xxxk("选修选课"),
		Bxqjhxk("本学期计划选课"),
		Knjxk("专业内跨年级选课"),
		Fawxk("跨专业选课"),
		Ggxxkxk("共选课选课");
		private String name;
		private CourseRepo(String name) {
			this.name = name;
		}
		public String getName() {
			return name;
		}
	};
	
	public CourseData() {
		this("", "");
	}
	
	public CourseData(String user, String pass) {
		course = new JsonObject();
		selected = new JsonArray();
		url = "http://jwxt.sustc.edu.cn/jsxsd";
		username = user;
		password = pass;
		try {
			fileOper(coursestorge, false);
			fileOper(selectedstorge, false);
			logger.info("Load storage.");
		} catch (FileNotFoundException e) {
			logger.warn("Update Data Failed: " + e.getMessage());
		} catch (IOException e) {
			logger.fatal(e.getMessage());
			System.exit(-1);
		}
	}
		
	public void getIn() throws AuthenticationException, StatusException {//获取选课权限
		CloseableHttpResponse response = null;
		if (isLogin()) {
			try {
				response = dataFetcher(Method.GET, Xsxk);
				if (EntityUtils.toString(response.getEntity()).contains("不在选课时间范围内")) {
					throw new StatusException("尚未开放选课");
				}
			} catch (IOException | ParseException e) {
				logger.error(e.getMessage());
			} finally {
				try {
					response.close();
				} catch (IOException e) {
					logger.error(e.getMessage());
				}
			}
		} else {
			login();
			getIn();
		}
	}
	
	public JsonObject updateCourseData() throws AuthenticationException, StatusException {//更新课程数据
		getIn();
		for (CourseRepo repo : CourseRepo.values()) {
			course.add(repo.name(), updateCourseData1(repo));		
		}
		return course;
	}
	
	private JsonArray updateCourseData1(CourseRepo repo) throws AuthenticationException { //获取课程数据
		try {
			CloseableHttpResponse response;
			JsonParser parse;
			JsonObject source;
			response = dataFetcher(Method.POST, 
					String.format(query, repo.name()),
					new String[] { "iDisplayStart=0", 
							"iDisplayLength=0" });
			if (response.getStatusLine().getStatusCode() == 200) {
				parse = new JsonParser(); //创建json解析器
				source = (JsonObject) parse
					.parse(new StringReader(EntityUtils.toString(response.getEntity()))); //创建jsonObject对象
				response.close();//获取总课程数
				response = dataFetcher(Method.POST, 
						String.format(query, repo.name()),
						new String[] {
								"iDisplayStart=0", 
								"iDisplayLength=" + source.get("iTotalRecords").getAsString() });
				source = (JsonObject) parse.parse(new StringReader(EntityUtils.toString(response.getEntity()))); //创建jsonObject对象
				response.close();//获取全部课程并写入source
				return source.get("aaData").getAsJsonArray();
			} else {
				logger.error(String.format("Failed to update %s, ignore it.\n", repo));
				return null;
			}
		} catch (ParseException | IOException | NullPointerException e) {
			logger.error(e.getMessage());
		}
		return null;
	}
	
	public JsonArray updateSelected() throws AuthenticationException, StatusException { //更新已选课程数据
		getIn();
		CloseableHttpResponse response;
		try {
			response = dataFetcher(Method.GET, Xkjglb, null);
			JsonArray array = new JsonArray();
			String string = EntityUtils.toString(response.getEntity());
			Document document = Jsoup.parse(string);
			Elements courses = document.getElementsByTag("tbody").get(0).children();
			for (int i = 0; i < courses.size(); i++) {
				String id = courses.get(i).child(10).child(0).id().split("_")[1];
				array.add(id);
			}
			selected = array;
		} catch (ParseException | IOException | IndexOutOfBoundsException | NullPointerException e) {
			logger.error(e.getMessage());
		}
		return selected;
	}
	
	private void fileOper(String FilePath, boolean work) throws FileNotFoundException, IOException { //写入到文件
		File file =new File(FilePath);
		if (work) {
			if(!file.exists()){
				file.createNewFile();
			}
			//true = append file
			FileWriter fileWritter = new FileWriter(file.getName(),false);
			JsonWriter writer = new JsonWriter(fileWritter);
			writer.setLenient(true);
			writer.setIndent("  ");
			if (FilePath.equals(coursestorge)) {
				Streams.write(course, writer);			
			} else if (FilePath.equals(selectedstorge)){
				Streams.write(selected, writer);
			}
			writer.flush();
			writer.close();
		}
		else {
			if(file.exists()){
				FileReader reader = new FileReader(file);
				JsonParser parse = new JsonParser();  //创建json解析器
				if (FilePath.equals(coursestorge)) {
					course = parse.parse(reader).getAsJsonObject();  //创建jsonObject对象					
				} else if(FilePath.equals(selectedstorge)) {
					selected = parse.parse(reader).getAsJsonArray();
				}
				reader.close();
			}
			else {
				throw new FileNotFoundException(String.format("Can't find '%s'\n", FilePath));
			}
		}
	}

	public void saveToFile() throws IOException, AuthenticationException, StatusException {
		fileOper(coursestorge, true);
		fileOper(selectedstorge, true);
	}
	
	public JsonObject getCourse() {
		return course;
	}
	
	public JsonArray getSelected() {
		return selected;
	}
	
	public JsonObject search(String name) { //查找课程
		JsonObject result = new JsonObject();
		for (Entry<String, JsonElement> entry : course.entrySet()) {
			JsonArray array = entry.getValue().getAsJsonArray();
			JsonArray tArray = new JsonArray();
			for (int j = 0; j < array.size(); j++) {
				JsonObject jsonObject = array.get(j).getAsJsonObject();
				if (jsonObject.toString().contains(name)) {
					tArray.add(jsonObject);
				}
			}
			result.add(entry.getKey(), tArray);
		}
		return result;
	}
	
	@Override
	public void login() throws AuthenticationException {
		login(username, password);
	}
	
	public void login(String user, String pass) throws AuthenticationException {
		username = user;
		password = pass;
		super.login();
	}
	
}
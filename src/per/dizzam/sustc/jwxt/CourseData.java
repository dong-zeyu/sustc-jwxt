package per.dizzam.sustc.jwxt;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;

import org.apache.http.HttpResponse;
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
import com.google.gson.JsonSyntaxException;
import com.google.gson.internal.Streams;
import com.google.gson.stream.JsonWriter;

import per.dizzam.sustc.cas.Method;
import per.dizzam.sustc.cas.NetworkConnection;

public class CourseData extends NetworkConnection {
	
	private Logger logger = Logger.getLogger("CourseCenter");
	
	JsonObject course;
	public JsonArray selected;
	
	private final String coursestorge = "course.json";
	private final String selectedstorge = "selected.json";
	
	private String Xsxk = "/xsxk/xsxk_index?jx0502zbid=";
	private String xklc_list = "/xsxk/xklc_list";
	private boolean isChoose = false;
	
	private final String Xkjglb = "/xsxkjg/comeXkjglb";	//已选课程
	
	private final String query = "/xsxkkc/xsxk%s?kcxx=&skls=&skxq=&skjc=&sfym=false&sfct=false";
	
	private static final String xsskOper = "/xsxkkc/%sOper?jx0404id=%s&xkzy=&trjf=";
	//选课参数： /xsxkkc/[课程库(小写)]Oper?jx0404id=[课程ID]&xkzy=&trjf="
	
	private final String XstkOper = "/xsxkjg/xstkOper?jx0404id=";	//学生退课
	//退课参数：/xsxkjg/xstkOper?jx0404id=[课程ID]"
	
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
			logger.fatal(e.getMessage(), e);
			System.exit(-1);
		}
	}
		
	public void getIn() throws AuthenticationException, StatusException {//获取选课权限
		if (isLogin()) {
 			try {
 				getIndex(); // XXX it's a very bad idea to put getIndex here
				CloseableHttpResponse response = dataFetcher(Method.GET, Xsxk);
 				if (EntityUtils.toString(response.getEntity()).contains("不在选课时间范围内")) {
					response.close();
 					throw new StatusException("尚未开放选课");
 				}
 			} catch (IOException | ParseException e) {
 				logger.error(e.getMessage(), e);
 			}
		} else {
			login();
			getIn();
		}
	}
	
	public void getIndex() throws StatusException, AuthenticationException, IOException {
		try {
			getIndex(0);
		} catch (IndexOutOfBoundsException | NullPointerException e) {
			throw new StatusException("尚未开放选课", e);
		}
	}
	
	public void getIndex(int index) throws NullPointerException, IndexOutOfBoundsException, AuthenticationException, IOException {
		if (!isChoose) {
			CloseableHttpResponse response = dataFetcher(Method.GET, xklc_list);
			Document document = Jsoup.parse(EntityUtils.toString(response.getEntity()));
			response.close();
			String id = document.getElementById("tbKxkc").child(0)
					.child(index + 1).child(0).lastElementSibling()
					.child(0).attr("href").split("=")[1];
			Xsxk += id;
			isChoose = true;			
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
			logger.error(e.getMessage(), e);
		}
		return null;
	}
	
	public JsonArray updateSelected() throws AuthenticationException, StatusException { //更新已选课程数据
		getIn();
		CloseableHttpResponse response;
		try {
			response = dataFetcher(Method.GET, Xkjglb, null);
			String string = EntityUtils.toString(response.getEntity());
			Document document = Jsoup.parse(string);
			Elements courses = document.getElementsByTag("tbody").get(0).children();
			for (int i = 0; i < courses.size(); i++) {
				String id = courses.get(i).child(10).child(0).id().split("_")[1];
				boolean flag = false;
				for (JsonElement jsonElement : selected) {
					if (jsonElement.getAsJsonObject().get("id").getAsString().equals(id)) {
						jsonElement.getAsJsonObject().addProperty("status", true);
						flag = true;
						break;
					}
				}
				if (flag) {
					JsonObject jsonObject = new JsonObject();
					jsonObject.addProperty("id", id);
					jsonObject.addProperty("status", true);
					selected.add(jsonObject);
				}
			}
		} catch (ParseException | IOException | IndexOutOfBoundsException | NullPointerException e) {
			logger.error(e.getMessage(), e);
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
	
	public JsonObject select0(String base, String id) throws AuthenticationException, IOException, StatusException {
		getIn();
		HttpResponse response = dataFetcher(Method.GET, 
				String.format(xsskOper, base.toLowerCase(), id));
		JsonParser jsonParser = new JsonParser();
		try {
			String string = EntityUtils.toString(response.getEntity());
			JsonObject source = (JsonObject) jsonParser
					.parse(new StringReader(string));//创建jsonObject对象
			JsonElement m = source.get("message");
			String msg = m == null ? "null" : m.getAsString();
			if (source.get("success").getAsBoolean() || msg.contains("已选择")) {
				source.addProperty("success", true);
				logger.info(String.format("Success in %s: %s", id, msg));
			} else {
				logger.info(String.format("Failed in %s: %s", id, msg));
			}
			return source;
		} catch (JsonSyntaxException e) {
			JsonObject msg = new JsonObject();
			msg.addProperty("success", false);
			msg.addProperty("message", "Internal server error!");
			logger.warn(String.format("Failed in %s: Internal server error!", id));
			return msg;
		} catch (ParseException | IOException | NullPointerException e) {
			JsonObject msg = new JsonObject();
			msg.addProperty("success", false);
			msg.addProperty("message", "Unknown error!");
			logger.error(String.format("Failed in %s: Unknown error!", id), e);
			return msg;
		}
	}
	
	public boolean select(String base, String id) throws AuthenticationException, StatusException, IOException { //选课操作
		getIn();
		HttpResponse response = dataFetcher(Method.GET, 
				String.format(xsskOper, base.toLowerCase(), id));
		JsonParser jsonParser = new JsonParser();
		try {
			String string = EntityUtils.toString(response.getEntity());
			JsonObject source = (JsonObject) jsonParser
					.parse(new StringReader(string));//创建jsonObject对象
			JsonElement m = source.get("message");
			String msg = m == null ? "null" : m.getAsString();
			if (source.get("success").getAsBoolean() || msg.contains("已选择")) {
				logger.info(String.format("Success in %s: %s", id, msg));
				return true;
			} else {
				logger.info(String.format("Failed in %s: %s", id, msg));
				return false;				
			}
		} catch (JsonSyntaxException e) {
			logger.warn(String.format("Failed in %s: Internal server error!", id));
		} catch (ParseException | IOException | NullPointerException e) {
			logger.error(e.getMessage(), e);
		}
		return false;
	}
	
	public boolean quit(String id) throws AuthenticationException, StatusException, IOException { //退课操作
		getIn();
		CloseableHttpResponse response = dataFetcher(Method.GET, 
				XstkOper + id);
		JsonParser jsonParser = new JsonParser();
		try {
			String string = EntityUtils.toString(response.getEntity());
			response.close();
			JsonObject source = (JsonObject) jsonParser.parse(new StringReader(string));//创建jsonObject对象
			
			if (source.get("success").getAsBoolean()) {
				logger.info("Succeed: 退课成功");
				return true;
			} else {
				logger.info("Failed: " + source.get("message").getAsString());
				return false;
			}
		} catch (JsonSyntaxException e) {
			logger.warn(String.format("Failed in %s: Internal server error!", id));		
		} catch (IOException | ParseException | NullPointerException e) {
			logger.error(e.getMessage(), e);
		}
		return false;
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

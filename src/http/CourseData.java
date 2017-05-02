package http;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;

import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import com.google.gson.JsonArray;
import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

public class CourseData extends NetworkConnection {
	JsonObject course;
	JsonArray selected;
	public JsonObject searchResult;
	
	private final String coursestorge = "course.json";
	private final String selectedstorge = "selected.json";
	
//	private final String Xsxk = "/xsxk/xsxk_index?jx0502zbid=054B5FA7E55F44E0BB3D24DB3BC561D5";	//主页，用于打开主页权限
	private final String Xsxk = "/xsxk/xsxk_index?jx0502zbid=054B5FA7E55F44E0BB3D24DB3BC561";	//主页，用于打开主页权限
//	private final String Xkjglb = "/xsxkjg/xsxkBxqjhxk";	//已选课程----I non't know why I write this, but I'll keep it before I confirm it is unused.
	private final String Xkjglb = "/xsxkjg/comeXkjglb";	//已选课程
	
	private final String query = "/xsxkkc/xsxk%s?kcxx=&skls=&skxq=&skjc=&sfym=false&sfct=false";
	
	public static enum CourseRepo {
		Bxxk, 	//必修选课
		Xxxk, 	//选修选课
		Bxqjhxk, 	//本学期计划选课
		Knjxk, 	//专业内跨年级选课
		Fawxk, 	//跨专业选课
		Ggxxkxk	//共选课选课
	};
	
	private static final String xsskOper = "/xsxkkc/%sOper?jx0404id=%s&xkzy=&trjf=";
	//选课参数： /xsxkkc/[课程库(小写)]Oper?jx0404id=[课程ID]&xkzy=&trjf="
	
	private final String XstkOper = "/xsxkjg/xstkOper?jx0404id=";	//学生退课
	//退课参数：/xsxkjg/xstkOper?jx0404id=[课程ID]"
	
	public CourseData(String user, String pass) {
		course = new JsonObject();
		selected = new JsonArray();
		searchResult = new JsonObject();
		url = "http://jwxt.sustc.edu.cn/jsxsd";
		username = user;
		password = pass;
	    if (fileOper(coursestorge, false) && fileOper(selectedstorge, false)) {
	    	System.out.println("[CourseCenter] Load storage.");
	   	}
	   	else {
	   		System.out.println("[CourseCenter] Not all storages are found, get them.");
	   		try {
				if (updateData()) {
					System.out.println("[CourseCenter] Setup course storage successfully.");
				} else {
					System.out.println("[CourseCenter] Can't get storage, exit immediately!");
					System.exit(-1);
				}
			} catch (Exception e) {
				if (e.getMessage() == "Can't get data whatever!") {
						System.out.println("[CourseCenter] Can't get storage, exit immediately!");
						System.exit(-1);					
				}
			}
    	}
	}
		
	private boolean getIn() throws Exception {//获取选课权限
		CloseableHttpResponse response = null;
		if (isLogin()) {
			try {
				response = dataFetcher(Method.GET, Xsxk);
				if (EntityUtils.toString(response.getEntity()).contains("未开放选课")) {
					System.out.println("[CourseCenter] 尚未开放选课");
					return false;
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				try {
					response.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			return true;
		} else {
			if (login()) {
				return getIn();
			}
			else {
				System.out.println("[CourseCenter] Can't get course center access: Login Failed!");
			}
		}
		return false;
	}
	
	private JsonArray getCourseData(CourseRepo repo) throws Exception { //获取课程数据
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
				System.out.printf("[CourseCenter] Failed to update %s, ignore it.\n", repo);
				return null;
			}
		} catch (ParseException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	private JsonArray getSelectedData() throws Exception { //更新已选课程数据
		CloseableHttpResponse response;
		response = dataFetcher(Method.GET, Xkjglb, null);
		JsonArray array = new JsonArray();
		try {
			String string = EntityUtils.toString(response.getEntity());
			Document document = Jsoup.parse(string);
			Elements courses = document.getElementsByTag("tbody").get(0).children();
			for (int i = 0; i < courses.size(); i++) {
				JsonObject object = new JsonObject();
				object.addProperty("ID", courses.get(i).child(0).text());
				object.addProperty("Name", courses.get(i).child(1).text());
				object.addProperty("Credit", courses.get(i).child(2).text());
				object.addProperty("Teacher", courses.get(i).child(4).text());
				object.addProperty("Time", courses.get(i).child(5).text());
				object.addProperty("Position", courses.get(i).child(6).text());
				object.addProperty("jx0404id", courses.get(i).child(9).child(0).id().split("_")[1]);
				array.add(object);
			}
			return array;
		} catch (ParseException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	private boolean fileOper(String FilePath, boolean work) { //写入到文件
		File file =new File(FilePath);
		try {
		if (work) {
			if(!file.exists()){
		   		file.createNewFile();
		   	}
			//true = append file
			FileWriter fileWritter = new FileWriter(file.getName(),false);
			BufferedWriter bufferWritter = new BufferedWriter(fileWritter);
			if (FilePath.equals(coursestorge)) {
				bufferWritter.write(course.toString());				
			} else if (FilePath.equals(selectedstorge)){
				bufferWritter.write(selected.toString());				
			}
			bufferWritter.close();
			return true;
		}
		else {
	        BufferedReader reader = null;
		   	if(file.exists()){
		        reader = new BufferedReader(new FileReader(file));
				JsonParser parse = new JsonParser();  //创建json解析器
				if (FilePath.equals(coursestorge)) {
					course = parse.parse(reader.readLine()).getAsJsonObject();  //创建jsonObject对象					
				} else if(FilePath.equals(selectedstorge)) {
					selected = parse.parse(reader.readLine()).getAsJsonArray();
				}
		      	reader.close();
		      	return true;
			}
		   	else {
				return false;
			}
		}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}
	
	public boolean updateData() throws Exception {//更新课程数据
		if (!getIn()) {
			System.out.println("[CourseCenter] Update Failed: Can't get course center access.");
			return false;
		}
		selected = getSelectedData();
		for (CourseRepo repo : CourseRepo.values()) {
			course.add(repo.name(), getCourseData(repo));		
		}
		if(fileOper(coursestorge, true) && fileOper(selectedstorge, true)) {
			return true;
		}
		return false;
	}
	
	public boolean select(String base, String id) throws Exception { //选课操作
		getIn();
		HttpResponse response = dataFetcher(Method.GET, 
				String.format(xsskOper, base.toLowerCase(), id));
		JsonParser jsonParser = new JsonParser();
		try {
			String string = EntityUtils.toString(response.getEntity());
			JsonObject source = (JsonObject) jsonParser
					.parse(new StringReader(string));//创建jsonObject对象
			if (source.get("success").getAsBoolean()) {
				System.out.printf("[CourseCenter] Success in %s: %s\n", id, source.get("message").getAsString());
				return true;
			} else {
				System.out.printf("[CourseCenter] Failed in %s: %s\n", id, source.get("message").getAsString());
				return false;
			}
		} catch (JsonIOException | JsonSyntaxException | ParseException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}
	
	public boolean quit(String id) throws Exception { //退课操作
		getIn();
		CloseableHttpResponse response = dataFetcher(Method.GET, 
				XstkOper + id);
		JsonParser jsonParser = new JsonParser();
		try {
			String string = EntityUtils.toString(response.getEntity());
			response.close();
			JsonObject source = (JsonObject) jsonParser.parse(new StringReader(string));//创建jsonObject对象
			if (source.get("success").getAsBoolean()) {
				System.out.println("[CourseCenter] Succeed: 退课成功");
				return true;
			} else {
				System.out.printf("[CourseCenter] Failed: %s", source.get("message").getAsString());
				return false;
			}
		} catch (JsonIOException | JsonSyntaxException | ParseException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		return false;
	}
	
	public void search(String name) { //查找课程
		JsonObject result = new JsonObject();
		for (CourseRepo repo : CourseRepo.values()) {
			if (course.get(repo.name()).isJsonArray()) {
				JsonArray array = course.get(repo.name()).getAsJsonArray();
				JsonArray tArray = new JsonArray();
				for (int j = 0; j < array.size(); j++) {
					JsonObject jsonObject = array.get(j).getAsJsonObject();
					if (jsonObject.toString().contains(name)) {
						tArray.add(jsonObject);
					}
				}
				result.add(repo.name(), tArray);
			}
			else {
				result.add(repo.name(), new JsonArray());
			}
		}
		searchResult = result;
	}
	
	@Override
	public boolean login() {
		return login(username, password);
	}
	
	public boolean login(String user, String pass) {
		username = user;
		password = pass;
		if (super.login()) {
			return true;
		}
		return false;	
	}
	
}

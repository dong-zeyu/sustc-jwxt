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

public class CourseData extends NetworkConnetion {
	JsonObject course;
	JsonArray selected;
	public JsonObject searchResult;
	
	private final String coursestorge = "course.json";
	private final String selectedstorge = "selected.json";
	
	private final String Xsxk = "/jsxsd/xsxk/xsxk_index?jx0502zbid=054B5FA7E55F44E0BB3D24DB3BC561D5";	//主页，用于打开主页权限
//	private final String Xkjglb = "/jsxsd/xsxkjg/xsxkBxqjhxk";	//已选课程----I non't know why I write this, but I'll keep it before I confirm it is unused.
	private final String Xkjglb = "/jsxsd/xsxkjg/comeXkjglb";	//已选课程
	
	private final String DefaultQuery = "?kcxx=&skls=&skxq=&skjc=&sfym=false&sfct=false";
	public static final String[] courserepo = new String[] {
			"Bxxk", 	//必修选课
			"Xxxk", 	//选修选课
			"Bxqjhxk", 	//本学期计划选课
			"Knjxk", 	//本学期计划选课
			"Fawxk", 	//跨专业选课
			"Ggxxkxk"	//共选课选课
			};
	
	static final String opBxxk = "/jsxsd/xsxkkc/bxxkOper";		//必修选课操作
	static final String opXxxk = "/jsxsd/xsxkkc/xxxkOper";		//选修选课操作
	static final String opBxqjhxk = "/jsxsd/xsxkkc/bxqjhxkOper";	//本学期计划选课操作
	static final String opKnjxk = "/jsxsd/xsxkkc/knjxkOper";	//专业内跨年级选课操作
	static final String opFawxk = "/jsxsd/xsxkkc/fawxkOper";	//跨专业选课操作
	static final String opGxkxk = "/jsxsd/xsxkkc/ggxxkxkOper";	//共选课选课操作
	//选课参数： op[选课] + "?jx0404id=课程ID&xkzy=&trjf="
	
	private final String XstkOper = "/jsxsd/xsxkjg/xstkOper";	//学生退课
	//退课参数：XstkOper +　"?jx0404id=课程ID"
	
	public CourseData(String user, String pass) {
		course = new JsonObject();
		selected = new JsonArray();
		searchResult = new JsonObject();
		url = "http://jwxt.sustc.edu.cn";
		username = user;
		password = pass;
	    if (writeCourse(coursestorge, false) && writeCourse(selectedstorge, false)) {
	    	System.out.println("[CourseCenter] Load storage.");
	   	}
	   	else {
	   		System.out.println("[CourseCenter] Not all storages are found, get them.");
	   		if (updateData()) {
	   			System.out.println("[CourseCenter] Setup course storage successfully.");
	   		} else {
				System.out.println("[CourseCenter] Can't get storage, exit immediately!");
				System.exit(-1);
			}
    	}
	}
	
	private boolean getIn() {//获取选课权限
		CloseableHttpResponse response = null;
		if (isLogIn()) {
			try {
					response = dataFetcher(NetworkConnetion.GET, Xsxk, null);
					if (EntityUtils.toString(response.getEntity())
							.contains("当前未开放选课")) {
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
	
	private JsonArray getCourseData(String repo) { //获取课程数据
		try {
			getIn();
			CloseableHttpResponse response;
			JsonParser parse;
			JsonObject source;
			response = dataFetcher(NetworkConnetion.POST, 
					"/jsxsd/xsxkkc/xsxk" + repo + "?kcxx=&skls=&skxq=&skjc=&sfym=false&sfct=false",
					new String[] { "iDisplayStart=0", 
			"iDisplayLength=0" });
			if (response.getStatusLine().getStatusCode() == 200) {
				parse = new JsonParser(); //创建json解析器
				source = (JsonObject) parse
					.parse(new StringReader(EntityUtils.toString(response.getEntity()))); //创建jsonObject对象
				response.close();//获取总课程数
				response = dataFetcher(NetworkConnetion.POST, 
						"/jsxsd/xsxkkc/xsxk" + repo + DefaultQuery, new String[] {
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
	
	private JsonArray getSelectedData() { //更新已选课程数据
		getIn();
		CloseableHttpResponse response;
		response = dataFetcher(NetworkConnetion.GET, Xkjglb, null);
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
	
	private boolean writeCourse(String FilePath, boolean work) { //写入到文件
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
	
	public boolean updateData() {//更新课程数据
		if (!getIn()) {
			System.out.println("[CourseCenter] Update Failed: Can't get course center access.");
			return false;
		}
		selected = getSelectedData();
		for (int i = 0; i < courserepo.length; i++) {
			course.add(courserepo[i], getCourseData(courserepo[i]));		
		}
		if(writeCourse(coursestorge, true) && writeCourse(selectedstorge, true)) {
			return true;
		}
		return false;
	}
	
	public boolean select(String base, String id) {
		getIn();
		HttpResponse response = dataFetcher(NetworkConnetion.GET, 
				base + "?jx0404id=" + id + "&xkzy=&trjf=", null);
		JsonParser jsonParser = new JsonParser();
		try {
			String string = EntityUtils.toString(response.getEntity());
			JsonObject source = (JsonObject) jsonParser
					.parse(new StringReader(string));//创建jsonObject对象
			while (!source.get("success").getAsBoolean()) {
				System.out.printf("[CourseCenter] Failed: %s\n", source.get("message").getAsString());
				Thread.sleep(10000);
				getIn();
				response = dataFetcher(NetworkConnetion.GET, 
						opFawxk + "?jx0404id=" + id + "&xkzy=&trjf=", null);
				source = (JsonObject) jsonParser
						.parse(new StringReader(EntityUtils.toString(response.getEntity())));//创建jsonObject对象
			}
		} catch (JsonIOException | JsonSyntaxException | ParseException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		return false;
	}
	
	public boolean quit(String id) {
		getIn();
		HttpResponse response = dataFetcher(NetworkConnetion.GET, 
				XstkOper + "?jx0404id=" + id, null);
		JsonParser jsonParser = new JsonParser();
		try {
			String string = EntityUtils.toString(response.getEntity());
			JsonObject source = (JsonObject) jsonParser
					.parse(new StringReader(string));//创建jsonObject对象
			if (source.get("success").getAsBoolean()) {
				System.out.println("[CourseCenter] Succeed: 退课成功");
			} else {
				System.out.printf("[CourseCenter] Failed: %s", source.get("message").getAsString());
			}
		} catch (JsonIOException | JsonSyntaxException | ParseException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		return false;
	}
	
	public void search(String name) {
		JsonObject result = new JsonObject();
		for (int i = 0; i < courserepo.length; i++) {
			if (course.get(courserepo[i]).isJsonArray()) {
				JsonArray array = course.get(courserepo[i]).getAsJsonArray();
				JsonArray tArray = new JsonArray();
				for (int j = 0; j < array.size(); j++) {
					JsonObject jsonObject = array.get(j).getAsJsonObject();
					if (jsonObject.toString().contains(name)) {
						tArray.add(jsonObject);
					}
				}
				result.add(courserepo[i], tArray);
			}
			else {
				result.add(courserepo[i], new JsonArray());
			}
		}
		searchResult = result;
	}
	
	public boolean login(String user, String pass) {
		username = user;
		password = pass;
		login();
		return false;	
	}
	
}

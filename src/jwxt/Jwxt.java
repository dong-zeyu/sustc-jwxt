package jwxt;

import java.util.Scanner;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import http.CourseData;
import http.CourseData.CourseRepo;

public class Jwxt {		
	public static void main(String[] args) {
		Scanner scanner = new Scanner(System.in);
		System.out.print("Please input your username: ");
		String username = scanner.nextLine();
		System.out.print("Please input your password: ");
		String password = scanner.nextLine();
		CourseData course = new CourseData(username, password);		
		String string = "";
		String param = "";
		printHelp(true);
		while (!string.equals("exit")) {
			System.out.print(">");
			string = scanner.nextLine();
			if (string.contains(" ")) {
				string = string.replaceAll("\\s+", " ");
				try {
					param = string.split(" ")[1];
					string = string.split(" ")[0];
				} catch (ArrayIndexOutOfBoundsException e) {
					System.out.println("Unknown command: Type 'help' for more information.");
					string = "";
					param = "";
				}
			}
			else {
				param = "";
			}
			switch (string) {
			case "login" :
				try {
					course.login(param.split(":")[0], param.split(":")[1]);
				} catch (IndexOutOfBoundsException e1) {
					System.out.println("Format error: pleas use 'login user:pass' to re-login");
				}
				break;
			case "remove" :
				break;
			case "list" :
				break;
			case "add" :
				break;
			case "select" :
				break;
			case "search" :
				if (!param.equals("")) {
					course.search(param);
					printSearch(course.searchResult);
				} else {
					System.out.println("Please input search param");
				}
				break;
			case "help":
				printHelp(false);
				break;
			case "update":
				System.out.println("[Main] Updating...");
				try {
					if (course.updateData()) {
						System.out.println("[Main] Succeed");
					}
					else {
						System.out.println("[Main] Failed due to some reason.");
					}
				} catch (Exception e1) {
					System.out.println("[Main] Faild: " + e1.getMessage());
				}
				break;
			case "exit":
				break;
			default:
				if (!string.equals("")) {
					System.out.println("Unknown command: Print 'help' for more information.");
				}
				break;
			}
		}
		scanner.close();
	}
	
	public static void printSearch(JsonObject result) {
		for (CourseRepo course : CourseRepo.values()) {
			String repo = course.name();
			System.out.println(repo + ":");
			JsonArray array = result.get(repo).getAsJsonArray();
			for (int j = 0; j < array.size(); j++) {
				JsonObject object = array.get(j).getAsJsonObject();
				String skls = "";
				if (object.get("skls").toString().equals("null")) {
					skls = "无";
				} else {
					skls = object.get("skls").getAsString();
				}
				System.out.printf("%d\t%s\t%s\t%d\t%s\t%s\t%s\t%s\n", j, 
						object.get("kch").getAsString(),
						object.get("kcmc").getAsString(),
						object.get("xf").getAsInt(), 
						object.get("sksj").getAsString().replaceAll("<br>", "|"),
						skls, 
						object.get("skdd").getAsString().replaceAll("<br>", "|"),
						object.get("ctsm").getAsString());
			}
		}
	}
	
	public static void printHelp(boolean isMini) {
		if (isMini) {
//			System.out.println("This is a mini useless help.");
			System.out.print(
					  "\nUsage: command parameter\n"
					+ "command: update search add remove list select help exit\n\n"
					+ "If you input error username and password try 'login user:pass' to re-login\n"
					+ "type 'help' for more information\n"
					);
		}
		else {
//			System.out.println("This is a useless help.");
			System.out.print(
					  "Usage: command [parameter]\n\n"
					+ "commands:\n"
					+ "\tupdate\t\tupdate course data\n"
					+ "\tsearch content\tsearch content from repository\n"
					+ "\tadd repo:index\tadd the course in search result with repository at index to the 'to-be-selected list'\n"
					+ "\tremove index\tremove the course from the course list to be selected\n"
					+ "\tlist\t\tlist the course to be selected\n"
					+ "\tselect\t\tselect the course in the list\n"
					+ "\tlogin user:pass\tre-login with new user and pass\n"
					+ "\texit\t\texit the program.\n\n"
					+ "available repository:\n"
					+ "\tBxxk\t必修选课\n"
					+ "\tXxxk\t选修选课\n"
					+ "\tBxqjhxk\t本学期计划选课\n"
					+ "\tKnjxk\t本学期计划选课\n"
					+ "\tFawxk\t跨专业选课\n"
					+ "\tGgxxkxk\t共选课选课\n"
					+ "\n"
					+ "Note: \ta) only after running 'search' can you run 'add' to add from search result!\n"
					+ 		"\tb) after you exit the program, the your course list will be auto save to file.\n"
					+ "Example:\n"
					+ "\tsearch 陈懿茂\n"
					+ "\tadd Bxqjhxk:0\n"
					+ "\tselect\n"
					);
		}
	}
	
}

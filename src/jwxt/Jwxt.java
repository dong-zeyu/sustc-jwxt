package jwxt;

import java.util.Scanner;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import http.CourseData;

public class Jwxt {	
	static JsonObject searchResult = null;
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Scanner scanner = new Scanner(System.in);
		System.out.print("Please input your username: ");
		String username = scanner.nextLine();
		System.out.print("Please input your password: ");
		String password = scanner.nextLine();
		CourseData course = new CourseData(username, password);		
		String string = "initial_8dzulAHI44TITVTP";
		String param = "";
		while (!string.equals("exit")) {
			switch (string) {
			case "login" :
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
					searchResult = course.Search(param);
					for (int i = 0; i < CourseData.courserepo.length; i++) {
						String repo = CourseData.courserepo[i];
						System.out.println(repo + ":");
						JsonArray array = searchResult.get(repo).getAsJsonArray();
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
				} else {
					System.out.println("Please input search param");
				}
				break;
			case "initial_8dzulAHI44TITVTP" :
				PrintHelp(true);
				break;
			case "help":
				PrintHelp(false);
				break;
			case "update":
				System.out.println("[Main] Updating...");
				if (course.UpdateData()) {
					System.out.println("[Main] Succeed");
				}
				else {
					System.out.println("[Main] Failed due to some reason.");
				}
				break;
			default:
				if (!string.equals("")) {
					System.out.println("Unknown command: Print 'help' for more information.");
				}
				break;
			}
			System.out.print(">");
			string = scanner.nextLine();
			if (string.contains(" ")) {
				try {
					param = string.split(" ")[1];
					string = string.split(" ")[0];
				} catch (ArrayIndexOutOfBoundsException e) {
					// TODO Auto-generated catch block
					System.out.println("Unknown command: Type 'help' for more information.");
					string = "";
					param = "";
				}
			}
			else {
				param = "";
			}
		}
		scanner.close();
	}
	
	public static void PrintHelp(boolean mini) {
		if (mini) {
//			System.out.println("This is a mini useless help.");
			System.out.print(
					  "\nUsage: command [parameter]\n"
					+ "command: update search add remove list select help exit\n\n"
					+ "If you input error username and password try 'login user:pass' to re-login"
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
					+ "Note: \ta) only after 'search' can you ran 'add' from search result!\n"
					+ 		"\tb) after you exit the program, the your course list will be auto save to file.\n"
					+ "Example:\n"
					+ "\tsearch 陈懿茂\n"
					+ "\tadd Bxqjhxk:0\n"
					+ "\tselect\n"
					);
		}
	}
	
}

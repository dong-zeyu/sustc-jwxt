package jwxt;

import java.util.Scanner;

import javax.naming.directory.SearchResult;

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
							System.out.printf("%d\t%s\t%d\t%s\t%s\t%s\t%s\n", j, 
									object.get("kch").getAsString(),
									object.get("xf").getAsInt(), 
									object.get("sksj").getAsString().replaceAll("<br>", " "),
									object.get("skls").getAsString(), 
									object.get("skdd").getAsString().replaceAll("<br>", " "),
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
					System.out.println("Unknown command: Print 'help' for more information.");
					string = "";
					param = "";
				}
			}
			else {
				param = "";
			}
		}
	}
	
	public static void PrintHelp(boolean mini) {
		if (mini) {
			System.out.println("This is a mini useless help.");
		}
		else {
			System.out.println("This is a useless help.");
		}
	}
	
}

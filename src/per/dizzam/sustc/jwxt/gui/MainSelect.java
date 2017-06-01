package per.dizzam.sustc.jwxt.gui;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;
import java.util.function.Predicate;

import org.apache.http.auth.AuthenticationException;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import per.dizzam.sustc.cas.Method;
import per.dizzam.sustc.jwxt.CourseData;
import per.dizzam.sustc.jwxt.CourseData.CourseRepo;
import per.dizzam.sustc.jwxt.StatusException;

public class MainSelect {
	
	static {
		PropertyConfigurator.configure("log4j.properties");
	}
	public static Logger logger = Logger.getLogger("Select");
	
	public static byte[] lock = new byte[0];
	public static int SLEEP = 1000;

	public static CourseData courseData;
	enum Course {
//		羽毛球(CourseRepo.Ggxxkxk, 		"201720181000289"),
//		乐理与视唱练耳(CourseRepo.Ggxxkxk, 	"201720181001120"),
//		抽象代数(CourseRepo.Fawxk, 		"201720181000476"),
//		综合物理实验(CourseRepo.Fawxk, 		"201720181000535"),
//		数学分析精讲(CourseRepo.Fawxk, 		"201720181001137"),
//		概率论(CourseRepo.Fawxk, 			"201720181001155"),
//		毛概(CourseRepo.Knjxk, 			"201720181001203"),
		;
		CourseRepo repo;
		String id;
		Course(CourseRepo repo, String name) {
			this.repo = repo;
			this.id = name;
		}
	}
	
	public static void main(String[] args) throws Exception{
		class Task extends TimerTask {
			
			ArrayList<Course> courses;
			
			@Override
			public void run() {
				synchronized (lock) {
					logger.info("Begin!");
					while (true) {
						try {
							courseData.getIn();
							break;
						} catch (StatusException e1) {
						} catch (AuthenticationException e) {
						}
						try {
							Thread.sleep(1000);
						} catch (InterruptedException e) {
							break;
						}
					}
					while (!courses.isEmpty()) {
						courses.removeIf(new Predicate<Course>() {
							
							@Override
							public boolean test(Course t) {
								try {
									return courseData.select(t.repo.name(), t.id);
								} catch (Exception e) {
									logger.error("Failed due to Execption: " + e.getMessage() 
									+ "\n\tCaused by: " + e.getCause());
								}
								return false;
							}
						});
					}
					logger.info("Over!");
					lock.notify();
				}
			}
			
			public Task(ArrayList<Course> courses) {
				this.courses = courses;
			}
			
		}
		
		class Run extends TimerTask {

			@Override
			public void run() {
				SLEEP = 100;
			}
			
		}
		Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT+8:00"));
		calendar.set(2017, 4, 25, 21, 59, 59);
		
		courseData = new CourseData("11611716", "dzy19980909");
		courseData.login();
		ArrayList<Course> courses = new ArrayList<>(Arrays.asList(Course.values()));
		
		SimpleDateFormat format = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.ENGLISH);
		long shift = format.parse(courseData.dataFetcher(Method.GET, "/").getFirstHeader("Date").getValue()).getTime() - new Date().getTime();		
		synchronized (lock) {
			new Timer(true).schedule(new Task(courses), new Date(calendar.getTime().getTime() - 20000));
			new Timer(true).schedule(new Run(), new Date(calendar.getTime().getTime() - shift));
			lock.wait();
		}
		System.out.println("terminate");
	}
	
}

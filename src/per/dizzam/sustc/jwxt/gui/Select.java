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

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import per.dizzam.sustc.cas.Method;
import per.dizzam.sustc.jwxt.CourseData;
import per.dizzam.sustc.jwxt.CourseRepo;

public class Select {
	
	static {
		PropertyConfigurator.configure("log4j.properties");
	}
	public static Logger logger = Logger.getLogger("Select");
	
	public static byte[] lock = new byte[0];

	public static CourseData courseData;
	enum Course {
		摄影(CourseRepo.Ggxxkxk, "201720181001105"),
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
					logger.info("Begin");
					long time = new Date().getTime();
					while (!courses.isEmpty() && new Date().getTime() - time < 10*1000l) {
						courses.removeIf(new Predicate<Course>() {
							
							@Override
							public boolean test(Course t) {
								try {
									return courseData.select(t.repo.name(), t.id);
								} catch (Exception e) {
									logger.error("Failed due to Execption: " + e.getMessage(), e);
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
		
		Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT+8:00"));
		calendar.set(2017, 4, 26, 21, 10, 0);
		
		courseData = new CourseData("11611716", "dzy19980909");
		courseData.login();
		ArrayList<Course> courses = new ArrayList<>(Arrays.asList(Course.values()));
		
		SimpleDateFormat format = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.ENGLISH);
		long shift = format.parse(courseData.dataFetcher(Method.GET, "/").getFirstHeader("Date").getValue()).getTime() - new Date().getTime();		
		synchronized (lock) {
			new Timer(true).schedule(new Task(courses), new Date(calendar.getTime().getTime() - shift));
			lock.wait();
		}
		System.out.println("terminate");
	}
	
}

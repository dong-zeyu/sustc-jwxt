package per.dizzam.sustc.jwxt;

public enum CourseRepo {
	Bxxk("必修选课"), Xxxk("选修选课"), Bxqjhxk("本学期计划选课"), Knjxk("专业内跨年级选课"), Fawxk("跨专业选课"), Ggxxkxk("共选课选课");
	private String name;

	private CourseRepo(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}
}

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This is just a demo for you, please run it on JDK17
 * (some statements may be not allowed in lower version).
 * This is just a demo, and you can extend and implement functions
 * based on this demo, or implement it in a different way.
 */
public class OnlineCoursesAnalyzer {
  List<Course> courses = new ArrayList<>();

  public OnlineCoursesAnalyzer(String datasetPath) {
    BufferedReader br = null;
    String line;
    try {
      br = new BufferedReader(new FileReader(datasetPath, StandardCharsets.UTF_8));
      br.readLine();
      while ((line = br.readLine()) != null) {
        String[] info = line.split(",(?=([^\\\"]*\\\"[^\\\"]*\\\")*[^\\\"]*$)", -1);
        Course course = new Course(info[0], info[1], new Date(info[2]), info[3], info[4], info[5],
            Integer.parseInt(info[6]), Integer.parseInt(info[7]), Integer.parseInt(info[8]),
            Integer.parseInt(info[9]), Integer.parseInt(info[10]), Double.parseDouble(info[11]),
            Double.parseDouble(info[12]), Double.parseDouble(info[13]),
            Double.parseDouble(info[14]), Double.parseDouble(info[15]),
            Double.parseDouble(info[16]), Double.parseDouble(info[17]),
            Double.parseDouble(info[18]), Double.parseDouble(info[19]),
            Double.parseDouble(info[20]), Double.parseDouble(info[21]),
            Double.parseDouble(info[22]));
        courses.add(course);
      }
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      if (br != null) {
        try {
          br.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }
  }

  //1
  public Map<String, Integer> getPtcpCountByInst() {
    return courses.stream().collect(Collectors.groupingBy(
        Course::getInstitution, Collectors.summingInt(Course::getParticipants)));
  }

  //2
  public Map<String, Integer> getPtcpCountByInstAndSubject() {
    return courses.stream().collect(Collectors.groupingBy(
        s -> s.getInstitution() + "-" + s.getSubject(),
            Collectors.summingInt(Course::getParticipants)))
        .entrySet().stream().sorted(
            Collections.reverseOrder(Map.Entry.comparingByValue()))
        .collect(Collectors.toMap(
            Map.Entry::getKey, Map.Entry::getValue, (a, b) -> a, LinkedHashMap::new));
  }

  //3
  public Map<String, List<List<String>>> getCourseListOfInstructor() {
    ArrayList<Instructor> instructorsList = new ArrayList<>();
    for (Course cours : courses) {
      instructorsList = cours.splitInstructors(instructorsList);
    }
    return instructorsList.stream().collect(Collectors.toMap(
      Instructor::getName, Instructor::getBothCourse));
  }

  //4
  public List<String> getCourses(int topK, String by) {
    return courses.stream().sorted((o1, o2) -> {
      if (by.equals("participants")) {
        return o2.getParticipants() - o1.getParticipants();
      } else {
        return (int) (o2.getTotalHours() - o1.getTotalHours());
      }
    }).map(Course::getTitle).distinct().limit(topK).toList();
  }

  //5
  public List<String> searchCourses(
      String courseSubject, double percentAudited, double totalCourseHours) {
    return courses.stream().filter(s -> (s.getSubject().toLowerCase()
        .contains(courseSubject.toLowerCase())
        && s.getPercentAudited() >= percentAudited && s.getTotalHours() <= totalCourseHours))
        .map(Course::getTitle).distinct().sorted().toList();
  }

  //6
  public List<String> recommendCourses(int age, int gender, int isBachelorOrHigher) {
    Map<String, Double> averageAge = courses.stream().collect(Collectors
        .groupingBy(Course::getNumber, Collectors.averagingDouble(Course::getMedianAge)));
    Map<String, Double> averageMale = courses.stream().collect(Collectors
        .groupingBy(Course::getNumber, Collectors.averagingDouble(Course::getPercentMale)));
    Map<String, Double> averageDegree = courses.stream().collect(Collectors
        .groupingBy(Course::getNumber, Collectors.averagingDouble(Course::getPercentDegree)));
    List<Course> courses1 = courses.stream().peek(s -> {
      s.setAverageAge(averageAge.get(s.getNumber()));
      s.setAverageDegree(averageDegree.get(s.getNumber()));
      s.setAverageMale(averageMale.get(s.getNumber()));
    }).toList();
    Map<String, Double> map = new HashMap<>();
    return courses1.stream().collect(Collectors.toMap(Function.identity(), s ->
       Math.pow(age - s.getAverageAge(), 2) + Math.pow(gender * 100 - s.getAverageMale(), 2)
             + Math.pow(isBachelorOrHigher * 100 - s.getAverageDegree(), 2)))
      .entrySet().stream().sorted((o1, o2) -> {
        if (Objects.equals(o1.getValue(), o2.getValue()) && o1.getKey().getNumber()
            .equals(o2.getKey().getNumber())) {
          return o2.getKey().getLaunchDate().compareTo(o1.getKey().getLaunchDate());
        }
        if (Objects.equals(o1.getValue(), o2.getValue())
            && !o1.getKey().getNumber().equals(o2.getKey().getNumber())) {
          return o1.getKey().getTitle().compareTo(o2.getKey().getTitle());
        }
        return (int) (o1.getValue() - o2.getValue());
      }).filter(a -> map.put(a.getKey().getNumber(), a.getValue()) == null
        && map.put(a.getKey().getTitle(), a.getValue()) == null)
        .map(Entry::getKey).map(Course::getTitle).limit(10).toList();
  }

}

class Course {
  String institution;
  String number;
  Date launchDate;
  String title;
  String instructors;
  String subject;
  int year;
  int honorCode;
  int participants;
  int audited;
  int certified;
  double percentAudited;
  double percentCertified;
  double percentCertified50;
  double percentVideo;
  double percentForum;
  double gradeHigherZero;
  double totalHours;
  double medianHoursCertification;
  double medianAge;
  double percentMale;
  double percentFemale;
  double percentDegree;
  double averageAge;
  double averageMale;
  double averageDegree;



  public Course(String institution, String number, Date launchDate,
                String title, String instructors, String subject,
                int year, int honorCode, int participants,
                int audited, int certified, double percentAudited,
                double percentCertified, double percentCertified50,
                double percentVideo, double percentForum, double gradeHigherZero,
                double totalHours, double medianHoursCertification,
                double medianAge, double percentMale, double percentFemale,
                double percentDegree) {
    this.institution = institution;
    this.number = number;
    this.launchDate = launchDate;
    if (title.startsWith("\"")) {
      title = title.substring(1);
    }
    if (title.endsWith("\"")) {
      title = title.substring(0, title.length() - 1);
    }
    this.title = title;
    if (instructors.startsWith("\"")) {
      instructors = instructors.substring(1);
    }
    if (instructors.endsWith("\"")) {
      instructors = instructors.substring(0, instructors.length() - 1);
    }
    this.instructors = instructors;
    if (subject.startsWith("\"")) {
      subject = subject.substring(1);
    }
    if (subject.endsWith("\"")) {
      subject = subject.substring(0, subject.length() - 1);
    }
    this.subject = subject;
    this.year = year;
    this.honorCode = honorCode;
    this.participants = participants;
    this.audited = audited;
    this.certified = certified;
    this.percentAudited = percentAudited;
    this.percentCertified = percentCertified;
    this.percentCertified50 = percentCertified50;
    this.percentVideo = percentVideo;
    this.percentForum = percentForum;
    this.gradeHigherZero = gradeHigherZero;
    this.totalHours = totalHours;
    this.medianHoursCertification = medianHoursCertification;
    this.medianAge = medianAge;
    this.percentMale = percentMale;
    this.percentFemale = percentFemale;
    this.percentDegree = percentDegree;
  }

  public String getInstitution() {
    return institution;
  }

  public int getParticipants() {
    return participants;
  }

  public String getSubject() {
    return subject;
  }

  public String getInstructors() {
    return instructors;
  }

  public Date getLaunchDate() {
    return launchDate;
  }

  public double getMedianAge() {
    return medianAge;
  }

  public double getPercentDegree() {
    return percentDegree;
  }

  public double getPercentMale() {
    return percentMale;
  }

  public double getPercentFemale() {
    return percentFemale;
  }

  public String getNumber() {
    return number;
  }

  public void setAverageAge(double averageAge) {
    this.averageAge = averageAge;
  }

  public void setAverageDegree(double averageDegree) {
    this.averageDegree = averageDegree;
  }

  public void setAverageMale(double averageMale) {
    this.averageMale = averageMale;
  }

  public double getAverageAge() {
    return averageAge;
  }

  public double getAverageMale() {
    return averageMale;
  }

  public double getAverageDegree() {
    return averageDegree;
  }

  public ArrayList<Instructor> splitInstructors(ArrayList<Instructor> instructorList) {
    List<String> instructors = new ArrayList<>(Arrays.asList(this.instructors.split(", ")));
    for (String instructor : instructors) {
      if (instructorList.stream().noneMatch(s -> s.getName().equals(instructor))) {
        Instructor a = new Instructor(instructor);
        if (instructors.size() == 1) {
          a.setSinCourse(this);
        } else {
          a.setCoCourse(this);
        }
        instructorList.add(a);
      } else {
        instructorList = new ArrayList<>(instructorList.stream().map(s -> {
          if (!s.getName().equals(instructor)) {
            return s;
          }
          if (instructors.size() == 1 && !s.sinCourse.contains(this)) {
            s.setSinCourse(this);
            return s;
          }
          if (!s.coCourse.contains(this) && instructors.size() > 1) {
            s.setCoCourse(this);
          }
          return s;
        }).toList());
      }
    }
    return instructorList;
  }

  public String getTitle() {
    return title;
  }

  public double getTotalHours() {
    return totalHours;
  }

  public double getPercentAudited() {
    return percentAudited;
  }

  @Override
  public boolean equals(Object o) {
    return getTitle().equals(((Course) o).getTitle());
  }

}

class Instructor {
  String name;
  
  List<Course> sinCourse = new ArrayList<>();
  
  List<Course> coCourse = new ArrayList<>();
  
  public Instructor(String name) {
    this.name = name;
  }


  public String getName() {
    return name;
  }

  public void setCoCourse(Course coCourse) {
    this.coCourse.add(coCourse);
  }

  public void setSinCourse(Course sinCourse) {
    this.sinCourse.add(sinCourse);
  }

  public List<String> getCoCourse() {
    return coCourse.stream().map(Course::getTitle).toList();
  }

  public List<String> getSinCourse() {
    return sinCourse.stream().map(Course::getTitle).toList();
  }
  
  public List<List<String>> getBothCourse() {
    return Stream.of(getSinCourse().stream().sorted().toList(),
      getCoCourse().stream().sorted().toList()).toList();
  }
}
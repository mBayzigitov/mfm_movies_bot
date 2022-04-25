public class Person {

    private long personId;
    private String nameRu;
    private String webUrl;
    private int growth;
    private String birthday;
    private String birthplace;
    private int age;
    private String profession;

    public Person(long personId, String nameRu, String webUrl, int growth, String birthday, String birthplace, int age, String profession) {
        this.personId = personId;
        this.nameRu = nameRu;
        this.webUrl = webUrl;
        this.growth = growth;
        this.birthday = birthday;
        this.birthplace = birthplace;
        this.age = age;
        this.profession = profession;
    }

    public long getPersonId() {
        return personId;
    }

    public void setPersonId(long personId) {
        this.personId = personId;
    }

    public String getNameRu() {
        return nameRu;
    }

    public void setNameRu(String nameRu) {
        this.nameRu = nameRu;
    }

    public String getWebUrl() {
        return webUrl;
    }

    public void setWebUrl(String webUrl) {
        this.webUrl = webUrl;
    }

    public int getGrowth() {
        return growth;
    }

    public void setGrowth(int growth) {
        this.growth = growth;
    }

    public String getBirthday() {
        return birthday;
    }

    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getProfession() {
        return profession;
    }

    public void setProfession(String profession) {
        this.profession = profession;
    }

    public String getBirthplace() {
        return birthplace;
    }

    public void setBirthplace(String birthplace) {
        this.birthplace = birthplace;
    }
}

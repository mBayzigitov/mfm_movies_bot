public class Movie {

    private String nameRu;
    private int year;
    private String premiereRu;
    private long kinopoiskId;
    private float ratingKinopoisk;
    private float ratingImdb;
    private float ratingAwait;
    private float rating;
    private String posterUrl;
    private String description;
    private String filmLength;
    private long filmId;
    private String webUrl;

    public long getFilmId() {
        return filmId;
    }

    public void setFilmId(long filmId) {
        this.filmId = filmId;
    }

    public String getName() {
        return nameRu;
    }

    public void setName(String nameRu) {
        this.nameRu = nameRu;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public String getPremiereRu() {
        return premiereRu;
    }

    public void setPremiereRu(String premiereRu) {
        this.premiereRu = premiereRu;
    }

    public long getKinopoiskId() {
        return kinopoiskId;
    }

    public void setKinopoiskId(long kinopoiskId) {
        this.kinopoiskId = kinopoiskId;
    }

    public float getRatingKinopoisk() {
        return ratingKinopoisk;
    }

    public void setRatingKinopoisk(float ratingKinopoisk) {
        this.ratingKinopoisk = ratingKinopoisk;
    }

    public float getRatingImdb() {
        return ratingImdb;
    }

    public void setRatingImdb(float ratingImdb) {
        this.ratingImdb = ratingImdb;
    }

    public float getRatingAwait() {
        return ratingAwait;
    }

    public void setRatingAwait(float ratingAwait) {
        this.ratingAwait = ratingAwait;
    }

    public float getRating() {
        return rating;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }

    public String getPosterUrl() {
        return posterUrl;
    }

    public void setPosterUrl(String posterURL) {
        this.posterUrl = posterURL;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getFilmLength() {
        return filmLength;
    }

    public void setFilmLength(String filmLength) {
        this.filmLength = filmLength;
    }

    public String getWebUrl() {
        return webUrl;
    }

    public void setWebUrl(String webUrl) {
        this.webUrl = webUrl;
    }

    @Override
    public String toString() {
        return "Movie{" +
                "nameRu='" + nameRu + '\'' +
                ", year=" + year +
                ", premiereRu='" + premiereRu + '\'' +
                ", kinopoiskId=" + kinopoiskId +
                ", ratingKinopoisk=" + ratingKinopoisk +
                ", ratingImdb=" + ratingImdb +
                ", ratingAwait=" + ratingAwait +
                '}';
    }

}

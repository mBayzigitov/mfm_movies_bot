import lombok.SneakyThrows;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.MessageEntity;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.time.LocalDate;
import java.time.Period;
import java.util.*;
import java.util.stream.Collectors;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;


public class MoviesBot extends TelegramLongPollingBot {

    // the way to divide users' deeds
    HashMap<Long, BotState> userState = new HashMap<>();

    // map which contains premieres by dates
    HashMap<LocalDate, StringBuilder> premieresByDate = new HashMap<>();

    // creating parsing builder
    GsonBuilder builder = new GsonBuilder();
    Gson gson = builder.create();

    @Override
    public String getBotUsername() {
        return HiddenVariables.bot_tag;
    }

    @Override
    public String getBotToken() {
        return HiddenVariables.bot_token;
    }

    @Override
    public void onUpdateReceived(Update update) {

        // Handling buttons pressing
        if (update.hasCallbackQuery()) {

            handleCallback(update.getCallbackQuery());

        }

        // Handling commands or usual messages
        if (update.hasMessage()) {

            // if command
            if (update.getMessage().hasEntities()) {

                handleCommand(update.getMessage());

            }

            // if usual message
            if (!update.getMessage().isCommand()) {

                handleAnswer(update.getMessage());

            }

        }

    }

    @SneakyThrows
    public void handleCallback(CallbackQuery callbackQuery) {

        Message message = callbackQuery.getMessage();

        // receiving action from button
        String action = callbackQuery.getData();

        switch (action) {
            case "TODAY'S PREMIERES":
                sendFeedback(message, "⏱ Пожалуйста, подождите");
                StringBuilder moviesLine = showPremieres(message.getChatId());
                sendFeedback(message, moviesLine.toString());
                break;
            case "INFORMATION ABOUT THE MOVIE":
                userState.put(message.getChatId(), BotState.FILMINFO);
                sendFeedback(message, "Введите название фильма");
                break;
            case "PERSON":
                userState.put(message.getChatId(), BotState.PERSON);
                sendFeedback(message, "Введите имя");
                break;
            case "SIMILARS":
                userState.put(message.getChatId(), BotState.SIMILARS);
                sendFeedback(message, "Введите название фильма");
                break;
        }

    }

    @SneakyThrows
    public HttpResponse<JsonNode> sendRequest(String request) {

        return Unirest.get(request)
                .header(HiddenVariables.bot_key_name, HiddenVariables.api_key)
                .asJson();

    }

    @SneakyThrows
    public void sendFeedback(Message message, String messageForSending) {

        execute(
                SendMessage.builder()
                        .chatId(message.getChatId().toString())
                        .text(messageForSending)
                        .parseMode("HTML")
                        .build());

    }

    @SneakyThrows
    public Movie getMovieByID(long getID) {

        String request = HiddenVariables.films_request + getID;
        HttpResponse<JsonNode> response = sendRequest(request);
        return new Gson().fromJson(String.valueOf(response.getBody()), Movie.class);

    }

    @SneakyThrows
    public StringBuilder showPremieres(long userID) {

        System.out.println("|| Current premieres hashmap ||");
        System.out.println(premieresByDate.keySet());

        LocalDate date = LocalDate.now();

        if (premieresByDate.get(date) != null) {
            return premieresByDate.get(date);
        }

        LinkedList<LocalDate> datesToRemove = new LinkedList<>();

        for (LocalDate forDate: premieresByDate.keySet()) {
            Period period = Period.between(date, forDate);
            if (Math.abs(period.getDays()) > 2) { datesToRemove.add(forDate); }
        }

        for (LocalDate forDate: datesToRemove) {
            premieresByDate.remove(forDate);
        }

        List<Movie> premieres;

        String request = HiddenVariables.premieres_request1 + date.getYear() + HiddenVariables.premieres_request2 + date.getMonth();
        HttpResponse<JsonNode> response = sendRequest(request);
        MainModel mainModel = new Gson().fromJson(String.valueOf(response.getBody()), MainModel.class);

        // filtering the list of premieres with stream API (filmed less than 3 years ago, will be released soon, first 15 items)
        premieres = mainModel.getItems().stream()
                .filter(movie -> movie.getYear() >= date.getYear() - 3
                        && Integer.parseInt(movie.getPremiereRu().substring(8, 10)) >= date.getDayOfMonth())
                .limit(15)
                .collect(Collectors.toList());

        StringBuilder moviesLine = new StringBuilder();

        if (premieres.size() == 0) {
            moviesLine.append("\uD83E\uDD37\uD83C\uDFFC\u200D♂️ Ближайших премьер не найдено");
            return moviesLine;
        }

        moviesLine.append("\uD83C\uDD95 15 ближайших премьер в кинотеатрах");

        for (Movie movie: premieres) {

            Movie movieDetails = getMovieByID(movie.getKinopoiskId());

            moviesLine.append("\n\n\uD83D\uDD39 \"").append(movie.getName()).append("\" ");
            if (movieDetails.getYear() != 0) moviesLine.append("(").append(movieDetails.getYear()).append(")\n");
            moviesLine.append("Премьера в России: ").append(movie.getPremiereRu().substring(8))
                    .append(movie.getPremiereRu().substring(4, 7))
                    .append("-")
                    .append(movie.getPremiereRu().substring(0, 4))
                    .append("\n");
            if (movieDetails.getWebUrl() != null) moviesLine.append("Ссылка на <a href=\"").append(movieDetails.getWebUrl()).append("\">КиноПоиск</a>");

            if (movieDetails.getRatingKinopoisk() != 0) { moviesLine.append("\nКинопоиск ").append(movieDetails.getRatingKinopoisk()).append(" "); }
            if (movieDetails.getRatingImdb() != 0) { moviesLine.append("\nIMDB ").append(movieDetails.getRatingImdb()).append(" "); }
            if (movieDetails.getRatingAwait() != 0) { moviesLine.append("\nРейтинг ожиданий ").append(movieDetails.getRatingAwait()).append(" "); }

        }

        premieresByDate.put(date, moviesLine);
        return moviesLine;

    }

    @SneakyThrows
    public StringBuilder showSimilars(String searchSimilarsName) {

        StringBuilder similarsList = new StringBuilder();

        Movie searchSimilars = getMovieByName(searchSimilarsName);
        String request = HiddenVariables.films_request + searchSimilars.getKinopoiskId() + "/similars";
        HttpResponse<JsonNode> response = sendRequest(request);

        MainModel mainModel = new Gson().fromJson(String.valueOf(response.getBody()), MainModel.class);
        List<Movie> similars;

        // taking first 5 items of the list
        similars = mainModel.getItems().stream()
                .limit(5)
                .collect(Collectors.toList());

        if (similars.size() == 0) {
            similarsList.append("Похожих фильмов не найдено");
            return similarsList;
        }

        similarsList.append("Фильмы, похожие на \"").append(searchSimilars.getName()).append("\"");

        for (Movie movie: similars) {

            Movie movieDetails = getMovieByID(movie.getFilmId());

            similarsList.append("\n\n\uD83D\uDD39 \"").append(movie.getName()).append("\" ");

            if (movieDetails.getYear() != 0) { similarsList.append("(").append(movieDetails.getYear()).append(")").append("\n"); }

            if (movieDetails.getRatingKinopoisk() != 0) { similarsList.append("Кинопоиск ").append(movieDetails.getRatingKinopoisk()).append(" "); }
            if (movieDetails.getRatingImdb() != 0) { similarsList.append("IMDB ").append(movieDetails.getRatingImdb()).append(" "); }
            if (movieDetails.getWebUrl() != null) { similarsList.append("\n")
                    .append("Ссылка на <a href=\"").append(movieDetails.getWebUrl()).append("\">КиноПоиск</a>").append(" "); }

        }

        return similarsList;

    }

    @SneakyThrows
    public Movie getMovieByName(String getName) {

        // clearing the entered string (replacing spaces, converting to lower case, replacing uncorrect symbols)
        getName = getName.replaceAll(" ", "")
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^A-Za-zА-Яа-я0-9еЕёЁ]", "");

        String request = HiddenVariables.search_by_keyword_request1 + getName + HiddenVariables.search_by_keyword_request2;
        HttpResponse<JsonNode> response = sendRequest(request);

        String responseAnswer = response.getBody().toString();

        if (responseAnswer.contains("\"searchFilmsCountResult\":0"))  {
            return null;
        } else {
            long madeId = Long.parseLong(responseAnswer.substring(responseAnswer.indexOf("filmId") + 8,
                    responseAnswer.indexOf("filmId") + 15).replaceAll("[^\\d]", ""));
            return getMovieByID(madeId);
        }

    }

    @SneakyThrows
    public void handleCommand(Message message)  {

            Optional<MessageEntity> commandEntity =
                    message.getEntities().stream().filter(e -> "bot_command".equals(e.getType())).findFirst();

            if (commandEntity.isPresent()) {

                String command = message.getText().substring(commandEntity.get().getOffset(), commandEntity.get().getLength());

                switch (command) {
                    case "/start":
                        userState.put(message.getChatId(), BotState.STATIC);
                        System.out.println("User ID:" + message.getChatId() + " started working with mfm_movies_bot");
                        System.out.println("Current number of users: " + userState.size());
                        List<List<InlineKeyboardButton>> buttons = new ArrayList<>();
                        buttons.add(Collections.singletonList(InlineKeyboardButton.builder().text("\uD83D\uDCC5 Ближайшие премьеры")
                                .callbackData("TODAY'S PREMIERES").build()));
                        buttons.add(Collections.singletonList(InlineKeyboardButton.builder().text("\uD83C\uDFAC Информация о фильме")
                                .callbackData("INFORMATION ABOUT THE MOVIE").build()));
                        buttons.add(Collections.singletonList(InlineKeyboardButton.builder().text("\uD83C\uDFAD Информация об актёре или режиссёре")
                                .callbackData("PERSON").build()));
                        buttons.add(Collections.singletonList(InlineKeyboardButton.builder().text("\uD83D\uDD39 Похожие фильмы")
                                .callbackData("SIMILARS").build()));
                        execute(
                                SendMessage.builder()
                                        .chatId(message.getChatId().toString())
                                        .text("\uD83D\uDCAC Что я могу для вас сделать?")
                                        .replyMarkup(InlineKeyboardMarkup.builder().keyboard(buttons).build())
                                        .build());
                        break;
                    case "/premieres":
                        sendFeedback(message, "⏱ Пожалуйста, подождите");
                        StringBuilder moviesLine = showPremieres(message.getChatId());
                        execute(
                                SendMessage.builder()
                                        .chatId(message.getChatId().toString())
                                        .text(moviesLine.toString())
                                        .parseMode("HTML")
                                        .build());
                        break;
                    case "/filminfo":
                        userState.put(message.getChatId(), BotState.FILMINFO);
                        sendFeedback(message, "Введите название фильма");
                        break;
                    case "/person":
                        userState.put(message.getChatId(), BotState.PERSON);
                        sendFeedback(message, "Введите имя");
                        break;
                    case "/similars":
                        userState.put(message.getChatId(), BotState.SIMILARS);
                        sendFeedback(message, "Введите название фильма");
                        break;
                }

            }
    }

    @SneakyThrows
    public void handleAnswer(Message message) {

        String answer = message.getText();

        switch (userState.get(message.getChatId())) {
            case FILMINFO:
                sendFeedback(message, "⏱ Пожалуйста, подождите");
                answer = answer.replaceAll(" ", "");
                answer = answer.toLowerCase(Locale.ROOT);
                answer = answer.replaceAll("[^A-Za-zА-Яа-я0-9еЕёЁ]", "");
                Movie answerFilmInfo = getMovieByName(answer);
                if (answerFilmInfo == null) {
                    sendFeedback(message, "\uD83E\uDD37\uD83C\uDFFC\u200D♂️ Фильма с таким названием не обнаружено. Попробуйте ещё.");
                    userState.put(message.getChatId(), BotState.FILMINFO);
                    break;
                } else {
                    String movieRecieved = "\uD83C\uDFAC  \"" + answerFilmInfo.getName() + "\"" + " (" + answerFilmInfo.getYear() + ")\n\n" +
                            "▶ ️Длительность - " + answerFilmInfo.getFilmLength() + " мин.\n\n" +
                            "\uD83D\uDDD2 " + answerFilmInfo.getDescription() + "\n\n" +
                            "⭐️ Кинопоиск " + answerFilmInfo.getRatingKinopoisk() +
                            " | IMDB " + answerFilmInfo.getRatingImdb() + "\n\n" +
                            "\uD83D\uDD17 Ссылка на <a href=\"" + answerFilmInfo.getWebUrl() + "\">КиноПоиск</a>" + "\n\n";
                    sendFeedback(message, movieRecieved);
                }
                userState.put(message.getChatId(), BotState.STATIC);
            break;
            case PERSON:
                sendFeedback(message, "⏱ Пожалуйста, подождите");
                Person answerPersonInfo = getPersonByName(answer);
                if (answerPersonInfo == null) {
                    sendFeedback(message, "\uD83E\uDD37\uD83C\uDFFC\u200D♂️ Актёра или режиссёра с таким именем не обнаружено. Попробуйте ещё.");
                    userState.put(message.getChatId(), BotState.PERSON);
                    break;
                } else {

                    StringBuilder personRecieved = new StringBuilder();
                    personRecieved.append("\uD83D\uDD36 " + answerPersonInfo.getNameRu() + "\n\n");

                    if (answerPersonInfo.getProfession() != null) { personRecieved.append("\uD83C\uDFA6 " + answerPersonInfo.getProfession() + "\n\n"); }
                    if (answerPersonInfo.getBirthday() != null) { personRecieved.append("\uD83D\uDDD2 " + "Дата и место рождения: " + answerPersonInfo.getBirthday() + "\n"); }
                    if (answerPersonInfo.getBirthplace() != null) { personRecieved.append(answerPersonInfo.getBirthplace() + "\n"); }
                    else { personRecieved.append(", неизвестно\n"); }
                    if (answerPersonInfo.getAge() != 0) { personRecieved.append("\uD83D\uDCC8 Возраст - " + answerPersonInfo.getAge() + " лет\n"); }
                    if (answerPersonInfo.getGrowth() != 0) { personRecieved.append("Рост - " + answerPersonInfo.getGrowth() + " см\n\n"); }
                    if (answerPersonInfo.getWebUrl() != null) { personRecieved.append("\uD83D\uDD17 Ссылка на <a href=\"" + answerPersonInfo.getWebUrl() + "\">КиноПоиск</a>" + "\n\n"); }

                    sendFeedback(message, personRecieved.toString());

                }
                userState.put(message.getChatId(), BotState.STATIC);
                break;
            case SIMILARS:
                sendFeedback(message, "⏱ Пожалуйста, подождите");
                Movie answerSimilarFilmInfo = getMovieByName(answer);
                if (answerSimilarFilmInfo == null) {
                    sendFeedback(message, "\uD83E\uDD37\uD83C\uDFFC\u200D♂️ Фильма с таким названием не обнаружено. Попробуйте ещё.");
                    userState.put(message.getChatId(), BotState.SIMILARS);
                    break;
                } else {

                    StringBuilder movieListRecieved = showSimilars(answer);
                    sendFeedback(message, movieListRecieved.toString());

                }
                userState.put(message.getChatId(), BotState.STATIC);
            break;
            case STATIC: sendFeedback(message, "\uD83D\uDCAC Ни одна команда на данный момент не используется. Введите /start, чтобы начать.");
        }

    }

    @SneakyThrows
    public Person getPersonById(long getID) {

        String request = HiddenVariables.staff_request + getID;
        HttpResponse<JsonNode> response = sendRequest(request);
        return new Gson().fromJson(String.valueOf(response.getBody()), Person.class);

    }

    @SneakyThrows
    public Person getPersonByName(String getName) {

        // clearing the entered string (replacing spaces, converting to lower case, replacing uncorrect symbols)
        getName = getName.replaceAll(" ", "")
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^A-Za-zА-Яа-я0-9еЕёЁ]", "");

        String request = HiddenVariables.person_request1 + getName + HiddenVariables.person_request2;
        HttpResponse<JsonNode> response = sendRequest(request);

        String responseAnswer = response.getBody().toString();

        if (responseAnswer.contains("\"searchFilmsCountResult\":0"))  {
            return null;
        } else {
            long madeId = Long.parseLong(responseAnswer.substring(responseAnswer.indexOf("kinopoiskId") + 12, responseAnswer.indexOf("}"))
                    .replaceAll("[^\\d]", ""));
            return getPersonById(madeId);
        }

    }

    @SneakyThrows
    public static void main(String[] args) {

        MoviesBot bot = new MoviesBot();
        TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
        telegramBotsApi.registerBot(bot);

    }

}

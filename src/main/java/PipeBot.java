import org.telegram.telegrambots.TelegramApiException;
import org.telegram.telegrambots.TelegramBotsApi;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

public class PipeBot extends TelegramLongPollingBot {

    private List<String> lines;

    private String botToken;
    private String chatId;

    public PipeBot(String token, List<String> lines, String chatId) {
        this.botToken = token;
        this.lines = lines;
        if (chatId != null)
            this.chatId = chatId;
        message();
    }

    public PipeBot(List<String> lines, String chatId) {
        this.lines = lines;
        if (chatId != null)
            this.chatId = chatId;
        message();
    }

    public void message() {
        if (chatId != null && lines.size() != 0) {
            SendMessage sendMessageRequest = new SendMessage();
            sendMessageRequest.setText(this.lines.stream().collect(Collectors.joining("\n")));
            sendMessageRequest.setChatId(this.chatId);
            try {
                sendMessage(sendMessageRequest);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }
        System.exit(0);
    }

    @Override
    public String getBotToken() {
        if (this.botToken == null) {
            System.out.println("Please provide bot token:");
            this.botToken = new Scanner(System.in).nextLine();
        }
        return this.botToken;
    }

    @Override
    public void onUpdateReceived(Update update) {
        try {
            handleMessage(update);
        } catch (Exception e) {
        }
    }

    @Override
    public String getBotUsername() {
        return "PipeBot";
    }

    private void handleMessage(Update update) {
        Message message = update.getMessage();
        if (chatId == null) {
            chatId = String.valueOf(message.getChatId());
            try {
                File file = new File("/home/dominic/projects/telegrambots/pipe/owner");
                if (!file.exists())
                    file.createNewFile();
                FileWriter fileWriter = new FileWriter(file);
                PrintWriter printWriter = new PrintWriter(fileWriter);
                printWriter.write(chatId);
                printWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        System.out.printf("New ownership id set to %s.\n", this.chatId);
        System.exit(0);
    }

    public static void main(String... args) throws IOException {

        List<String> lines = new ArrayList<>();
        if (System.in.available() != 0) {
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            String temp;
            while ((temp = br.readLine()) != null)
                lines.add(temp);
        }
        String chatId = null;
        File chatIdFile = new File("/home/dominic/projects/telegrambots/pipe/owner");
        if (chatIdFile.exists())
            chatId = new Scanner(chatIdFile).nextLine();

        final PipeBot pipeBot;
        if (args.length > 0)
            pipeBot = new PipeBot(args[0], lines, chatId);
        else
            pipeBot = new PipeBot(lines, chatId);
        new Thread(() -> {
            TelegramBotsApi telegramBotsApi = new TelegramBotsApi();
            try {
                telegramBotsApi.registerBot(pipeBot);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }).start();
    }

}

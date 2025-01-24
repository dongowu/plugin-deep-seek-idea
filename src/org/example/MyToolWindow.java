package org.example;

import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBTextArea;
import com.intellij.ui.components.JBScrollPane;
import org.codehaus.jettison.json.JSONObject;
import org.jetbrains.annotations.NotNull;
import org.json.simple.JSONArray;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class MyToolWindow implements ToolWindowFactory {
    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        // 创建一个主面板，使用 BorderLayout
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // 设置边距

        // 初始化 API Key 验证界面
        showApiKeyVerification(mainPanel);

        // 将主面板添加到 Tool Window
        ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();
        Content content = contentFactory.createContent(mainPanel, "", false);
        toolWindow.getContentManager().addContent(content);
    }

    /**
     * 显示 API Key 验证界面
     *
     * @param parentPanel 父面板
     */
    private void showApiKeyVerification(JPanel parentPanel) {
        // 清空父面板
        parentPanel.removeAll();

        // 创建一个面板，用于放置 API Key 输入框和验证按钮
        JPanel apiKeyPanel = new JPanel();
        apiKeyPanel.setLayout(new BoxLayout(apiKeyPanel, BoxLayout.Y_AXIS));
        apiKeyPanel.setAlignmentX(Component.CENTER_ALIGNMENT); // 居中

        // 添加标题
        JBLabel titleLabel = new JBLabel("DeepSeek API Key Verification");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18)); // 设置字体
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        apiKeyPanel.add(titleLabel);
        apiKeyPanel.add(Box.createRigidArea(new Dimension(0, 10))); // 添加间距

        // 添加 API Key 输入框
        JBTextArea apiKeyField = new JBTextArea();
        apiKeyField.setMaximumSize(new Dimension(300, 30)); // 设置输入框大小
        apiKeyField.setAlignmentX(Component.CENTER_ALIGNMENT);
        apiKeyPanel.add(new JBLabel("API Key:"));
        apiKeyPanel.add(new JBScrollPane(apiKeyField));
        apiKeyPanel.add(Box.createRigidArea(new Dimension(0, 20))); // 添加间距

        // 添加验证按钮
        JButton verifyButton = new JButton("Verify API Key");
        verifyButton.setBackground(new Color(0, 120, 215)); // 设置按钮背景色
        verifyButton.setForeground(Color.WHITE); // 设置按钮文字颜色
        verifyButton.setAlignmentX(Component.CENTER_ALIGNMENT);

        // 添加按钮点击事件
        verifyButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String apiKey = apiKeyField.getText();
                if (verifyApiKey(apiKey)) {
                    // 验证成功，显示聊天界面
                    showChatInterface(parentPanel, apiKey);
                } else {
                    // 验证失败，显示错误信息
                    JOptionPane.showMessageDialog(parentPanel, "Invalid API Key. Please try again.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        apiKeyPanel.add(verifyButton);

        // 将 API Key 面板添加到父面板
        parentPanel.add(apiKeyPanel, BorderLayout.CENTER);
        parentPanel.revalidate();
        parentPanel.repaint();
    }

    /**
     * 显示聊天界面
     *
     * @param parentPanel 父面板
     * @param apiKey      验证通过的 API Key
     */
    private void showChatInterface(JPanel parentPanel, String apiKey) {
        // 清空父面板
        parentPanel.removeAll();

        // 创建一个主面板，使用 BorderLayout
        JPanel chatPanel = new JPanel(new BorderLayout());
        chatPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // 设置边距

        // 添加标题
        JBLabel chatTitleLabel = new JBLabel("DeepSeek Chat");
        chatTitleLabel.setFont(new Font("Arial", Font.BOLD, 18)); // 设置字体
        chatTitleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        chatPanel.add(chatTitleLabel, BorderLayout.NORTH);

        // 添加聊天记录区域
        JBTextArea chatArea = new JBTextArea();
        chatArea.setEditable(false);
        chatArea.setLineWrap(true);
        chatArea.setWrapStyleWord(true);
        JBScrollPane chatScrollPane = new JBScrollPane(chatArea);
        chatPanel.add(chatScrollPane, BorderLayout.CENTER);

        // 添加输入框和发送按钮
        JPanel inputPanel = new JPanel(new BorderLayout());
        JBTextArea inputField = new JBTextArea();
        inputField.setLineWrap(true);
        inputField.setWrapStyleWord(true);
        JBScrollPane inputScrollPane = new JBScrollPane(inputField);
        inputScrollPane.setPreferredSize(new Dimension(300, 80)); // 设置输入框大小
        JButton sendButton = new JButton("Send");
        inputPanel.add(inputScrollPane, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);

        // 添加发送按钮点击事件
        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String message = inputField.getText();
                if (!message.isEmpty()) {
                    // 显示用户消息
                    chatArea.append("You: " + message + "\n");

                    // 调用 DeepSeek API 获取回复
                    getDeepSeekReplyAsync(apiKey, message, null, chatArea);

                    // 清空输入框
                    inputField.setText("");
                }
            }
        });
        chatPanel.add(inputPanel, BorderLayout.SOUTH);

        // 将聊天面板添加到父面板
        parentPanel.add(chatPanel, BorderLayout.CENTER);
        parentPanel.revalidate();
        parentPanel.repaint();
    }

    /**
     * 验证 API Key
     *
     * @param apiKey 用户输入的 API Key
     * @return 验证是否成功
     */
    private boolean verifyApiKey(String apiKey) {
        try {
            URL url = new URL("https://api.deepseek.com/user/balance"); // DeepSeek 验证 API 地址
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Authorization", "Bearer " + apiKey);
            connection.setRequestProperty("Content-Type", "application/json");

            // 获取响应
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                // 读取响应内容
                try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                    StringBuilder response = new StringBuilder();
                    String responseLine;
                    while ((responseLine = br.readLine()) != null) {
                        response.append(responseLine.trim());
                    }

                    // 解析 JSON 数据
                    JSONObject jsonResponse = new JSONObject(response.toString());
                    if (jsonResponse.has("is_available")) {
                        boolean is_available = jsonResponse.getBoolean("is_available");
                        if (is_available == true) {
                            return true;
                        }
                        System.out.println("API Key is is_available. Balance: " + jsonResponse.getJSONArray("balance_infos"));
                    } else {
                        System.out.println("API Key is invalid. Response: " + response.toString());
                    }
                    return false;
                }
            } else {
                System.out.println("API Key verification failed. Response code: " + responseCode);
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 调用 DeepSeek API 获取回复
     *
     * @param apiKey  API Key
     * @param message 用户发送的消息
     * @return DeepSeek 的回复
     */
    private String getDeepSeekReply(String apiKey, String message) {
        try {
            URL url = new URL("https://api.deepseek.com/chat/completions"); // DeepSeek 聊天 API 地址
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Authorization", "Bearer " + apiKey);
            connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8"); // 明确指定字符编码
            connection.setDoOutput(true);

            // 构建请求体
            JSONObject requestBody = new JSONObject();
            requestBody.put("model", "deepseek-chat"); // 设置模型

            // 构建 messages 数组
            JSONArray messages = new JSONArray();

            // 添加系统消息
            JSONObject systemMessage = new JSONObject();
            systemMessage.put("role", "system");
            systemMessage.put("content", "You are a helpful assistant");
            messages.add(systemMessage);

            // 添加用户消息
            JSONObject userMessage = new JSONObject();
            userMessage.put("role", "user");
            userMessage.put("content", message);
            messages.add(userMessage);

            requestBody.put("messages", messages); // 添加消息列表

            // 添加其他参数
            requestBody.put("frequency_penalty", 0);
            requestBody.put("max_tokens", 2048);
            requestBody.put("presence_penalty", 0);

            JSONObject responseFormat = new JSONObject();
            responseFormat.put("type", "text");
            requestBody.put("response_format", responseFormat);

            requestBody.put("stop", JSONObject.NULL); // stop 为 null
            requestBody.put("stream", false);
            requestBody.put("stream_options", JSONObject.NULL); // stream_options 为 null
            requestBody.put("temperature", 1);
            requestBody.put("top_p", 1);
            requestBody.put("tools", JSONObject.NULL); // tools 为 null
            requestBody.put("tool_choice", "none");
            requestBody.put("logprobs", false);
            requestBody.put("top_logprobs", JSONObject.NULL); // top_logprobs 为 null

            // 发送请求
            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = requestBody.toString().getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            // 获取响应
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                try (BufferedReader br = new BufferedReader(
                        new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) { // 明确指定字符编码
                    StringBuilder response = new StringBuilder();
                    String responseLine;
                    while ((responseLine = br.readLine()) != null) {
                        response.append(responseLine.trim());
                    }

                    // 解析响应
                    JSONObject jsonResponse = new JSONObject(response.toString());
                    org.codehaus.jettison.json.JSONArray choices = jsonResponse.getJSONArray("choices");
                    if (choices.length() > 0) {
                        JSONObject firstChoice = choices.getJSONObject(0);
                        JSONObject replyMessage = firstChoice.getJSONObject("message");
                        return replyMessage.getString("content"); // 返回 DeepSeek 的回复内容
                    } else {
                        return "Error: No reply from DeepSeek.";
                    }
                }
            } else {
                // 读取错误信息
                try (BufferedReader br = new BufferedReader(
                        new InputStreamReader(connection.getErrorStream(), StandardCharsets.UTF_8))) { // 明确指定字符编码
                    StringBuilder errorResponse = new StringBuilder();
                    String errorLine;
                    while ((errorLine = br.readLine()) != null) {
                        errorResponse.append(errorLine.trim());
                    }
                    return "Error: " + errorResponse.toString();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "Error: " + e.getMessage();
        }
    }


    private void getDeepSeekReplyAsync(String apiKey, String message, Project project, JBTextArea chatArea) {
        new Task.Backgroundable(project, "Getting DeepSeek Reply", false) {
            @Override
            public void run(@NotNull com.intellij.openapi.progress.ProgressIndicator indicator) {
                String reply = getDeepSeekReply(apiKey, message);
                SwingUtilities.invokeLater(() -> chatArea.append("DeepSeek: " + reply + "\n"));
            }
        }.queue();
    }
}
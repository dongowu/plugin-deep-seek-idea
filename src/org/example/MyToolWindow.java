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

        // 创建一个主面板，使用 GridBagLayout 布局
        JPanel apiKeyPanel = new JPanel(new GridBagLayout());
        apiKeyPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20)); // 添加内边距

        // 设置 GridBagConstraints
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10); // 设置组件间距
        gbc.fill = GridBagConstraints.HORIZONTAL; // 组件水平填充
        gbc.anchor = GridBagConstraints.CENTER; // 组件居中

        // 加载 logo.png
        // 加载 logo.png
        URL logoUrl = getClass().getResource("/logo.png"); // 从 resources 目录加载
        if (logoUrl != null) {
            // 加载原始图片
            ImageIcon originalIcon = new ImageIcon(logoUrl);
            Image originalImage = originalIcon.getImage();

            // 原始图片的宽度和高度
            int originalWidth = originalIcon.getIconWidth();
            int originalHeight = originalIcon.getIconHeight();

            // 目标宽度
            int targetWidth = 200; // 设置目标宽度
            // 根据目标宽度计算目标高度（保持宽高比）
            int targetHeight = (int) ((double) originalHeight / originalWidth * targetWidth);

            // 缩放图片
            Image scaledImage = originalImage.getScaledInstance(targetWidth, targetHeight, Image.SCALE_SMOOTH);

            // 将缩放后的图片包装为 ImageIcon
            ImageIcon logoIcon = new ImageIcon(scaledImage);

            // 创建 JLabel 并添加到面板
            JLabel logoLabel = new JLabel(logoIcon);
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.gridwidth = 2; // 跨两列
            gbc.anchor = GridBagConstraints.CENTER; // 居中
            apiKeyPanel.add(logoLabel, gbc);
        } else {
            // 如果找不到 logo，显示提示信息
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.gridwidth = 2;
            apiKeyPanel.add(new JBLabel("Logo not found!"), gbc);
        }

        // 添加标题
        JBLabel titleLabel = new JBLabel("DeepSeek API Key Verification");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18)); // 设置字体
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        apiKeyPanel.add(titleLabel, gbc);

        // 添加 API Key 输入框
        JTextField apiKeyField = new JTextField();
        apiKeyField.setPreferredSize(new Dimension(300, 40)); // 设置输入框大小
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        apiKeyPanel.add(apiKeyField, gbc);

        // 添加验证按钮
        JButton verifyButton = new JButton("Verify");
        verifyButton.setPreferredSize(new Dimension(100, 40)); // 设置按钮大小
        verifyButton.setBackground(new Color(0, 120, 215)); // 按钮背景色
        verifyButton.setForeground(Color.WHITE); // 按钮文字颜色
        verifyButton.setFocusPainted(false); // 移除焦点边框
        verifyButton.setFont(new Font("Arial", Font.BOLD, 14)); // 设置字体
        verifyButton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0, 120, 215), 2, true), // 圆角边框
                BorderFactory.createEmptyBorder(5, 15, 5, 15) // 内边距
        ));
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        apiKeyPanel.add(verifyButton, gbc);

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

        // 将主面板添加到父面板
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

        // 创建聊天区域
        JPanel chatArea = new JPanel();
        chatArea.setLayout(new BoxLayout(chatArea, BoxLayout.Y_AXIS));
        chatArea.setBackground(new Color(240, 240, 240));

        // 将聊天区域包裹在一个滚动面板中
        JBScrollPane chatScrollPane = new JBScrollPane(chatArea);
        chatScrollPane.setBorder(BorderFactory.createEmptyBorder());
        chatPanel.add(chatScrollPane, BorderLayout.CENTER);

        // 添加初始提示消息
        addMessageBubble(chatArea, "DeepSeek Assistant", "你好，我是 DeepSeek 助理，输入你的问题吧。", false);

        // 创建输入面板
        JPanel inputPanel = new JPanel(new BorderLayout());
        inputPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JTextArea inputField = new JTextArea();
        inputField.setLineWrap(true);
        inputField.setWrapStyleWord(true);
        inputField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));

        JBScrollPane inputScrollPane = new JBScrollPane(inputField);
        inputScrollPane.setPreferredSize(new Dimension(300, 80));

        JButton sendButton = new JButton("Send");
        sendButton.setBackground(new Color(0, 120, 215));
        sendButton.setForeground(Color.WHITE);
        sendButton.setFocusPainted(false);
        sendButton.setFont(new Font("Arial", Font.BOLD, 14));
        sendButton.setPreferredSize(new Dimension(100, 35));

        inputPanel.add(inputScrollPane, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);

        // 添加发送按钮点击事件
        sendButton.addActionListener(e -> {
            String message = inputField.getText().trim();
            if (!message.isEmpty()) {
                // 添加用户消息气泡
                addMessageBubble(chatArea, "You", message, true);

                // 调用 DeepSeek API 获取回复
                getDeepSeekReplyAsync(apiKey, message, null, chatArea);

                // 清空输入框
                inputField.setText("");
            }
        });

        chatPanel.add(inputPanel, BorderLayout.SOUTH);

        // 将聊天面板添加到父面板
        parentPanel.add(chatPanel, BorderLayout.CENTER);
        parentPanel.revalidate();
        parentPanel.repaint();
    }

    private void addMessageBubble(JPanel chatArea, String sender, String message, boolean isUser) {
        BubblePanel bubble = new BubblePanel(message, isUser);

        // 设置气泡对齐方式
        if (isUser) {
            bubble.setAlignmentX(Component.LEFT_ALIGNMENT); // 用户消息右对齐
        } else {
            bubble.setAlignmentX(Component.RIGHT_ALIGNMENT); // 助手消息左对齐
        }

        chatArea.add(bubble);
        chatArea.add(Box.createRigidArea(new Dimension(0, 10))); // 添加间距

        chatArea.revalidate();
        chatArea.repaint();

        // 滚动到底部
        JScrollBar verticalScrollBar = ((JBScrollPane) chatArea.getParent().getParent()).getVerticalScrollBar();
        verticalScrollBar.setValue(verticalScrollBar.getMaximum());
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


    private void getDeepSeekReplyAsync(String apiKey, String message, Project project, JPanel chatArea) {
        new Task.Backgroundable(project, "Getting DeepSeek Reply", false) {
            @Override
            public void run(@NotNull com.intellij.openapi.progress.ProgressIndicator indicator) {
                String reply = getDeepSeekReply(apiKey, message);
                SwingUtilities.invokeLater(() -> {
                    addMessageBubble(chatArea, "DeepSeek Assistant", reply, false); // 添加助手消息气泡
                });
            }
        }.queue();
    }
}
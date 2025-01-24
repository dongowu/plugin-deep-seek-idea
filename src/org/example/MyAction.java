package org.example;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import org.jetbrains.annotations.NotNull;

public class MyAction extends AnAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        // 在这里实现 Action 的逻辑
        Messages.showInfoMessage("Hello, this is my custom action!", "My Action");
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        // 在这里控制 Action 的可见性、启用状态等
        e.getPresentation().setEnabledAndVisible(true);
    }
}

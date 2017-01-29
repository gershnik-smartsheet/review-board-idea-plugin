/*
 * Copyright 2015 Ritesh Kapoor
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ritesh.idea.plugin.state;

import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.ritesh.idea.plugin.exception.InvalidConfigurationException;
import com.ritesh.idea.plugin.messages.PluginBundle;
import com.ritesh.idea.plugin.reviewboard.ReviewDataProvider;
import com.ritesh.idea.plugin.ui.ExceptionHandler;
import com.ritesh.idea.plugin.ui.TaskUtil;
import com.ritesh.idea.plugin.ui.panels.LoginPanel;
import com.ritesh.idea.plugin.util.ThrowableFunction;
import org.apache.commons.lang.mutable.MutableObject;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * @author Ritesh
 */
public class SettingsPage implements Configurable {

    public static final String SETTINGS_DISPLAY_NAME = "Review Board";

    private LoginPanel loginPanel = new LoginPanel();
    private Configuration oldConfigurationState;
    private Configuration currentConfigurationState;
    private Project project;

    public SettingsPage(Project project) {
        this.project = project;
    }

    @Nls
    @Override
    public String getDisplayName() {
        return SETTINGS_DISPLAY_NAME;
    }

    @Nullable
    @Override
    public String getHelpTopic() {
        return null;
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        oldConfigurationState = ConfigurationPersistance.getInstance(project).getState();
        if (oldConfigurationState != null) {
            currentConfigurationState = new Configuration(oldConfigurationState);
            loginPanel.setUrl(oldConfigurationState.url);
            loginPanel.setUsername(oldConfigurationState.username);
            loginPanel.setPassword(oldConfigurationState.password);
            loginPanel.setToken(oldConfigurationState.token);
            loginPanel.setUseToken(oldConfigurationState.useToken);
            loginPanel.setUseRbTools(oldConfigurationState.useRbTools);
            loginPanel.setUseRbToolPath(oldConfigurationState.rbtPath);
        } else {
            currentConfigurationState = new Configuration();
        }
        loginPanel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                testConnection();
            }
        });
        return loginPanel.getPanel();
    }

    @Override
    public boolean isModified() {
        fetchCurrentConfiguration();
        return currentConfigurationState.isValid() && !currentConfigurationState.equals(oldConfigurationState);
    }

    @Override
    public void apply() throws ConfigurationException {
        fetchCurrentConfiguration();
        ConfigurationPersistance.getInstance(project).saveState(currentConfigurationState);
        ReviewDataProvider.reset();
        oldConfigurationState = new Configuration(currentConfigurationState);
    }

    @Override
    public void reset() {

    }

    @Override
    public void disposeUIResources() {

    }

    private void testConnection() {

        fetchCurrentConfiguration();
        if (!currentConfigurationState.isValid()) {
            Messages.showErrorDialog(project, "Connection information provided is invalid.", "Invalid Settings");
            return;
        }

        final MutableObject connException = new MutableObject();
        TaskUtil.queueTask(project, PluginBundle.message(PluginBundle.CONNECTION_TEST_TITLE), true, new ThrowableFunction<ProgressIndicator, Void>() {
            @Override
            public Void throwableCall(ProgressIndicator params) throws Exception {
                try {
                    params.setIndeterminate(true);
                    ReviewDataProvider.testConnection(currentConfigurationState);
                    // The task was not cancelled and is successful
                    connException.setValue(Boolean.TRUE);
                } catch (InvalidConfigurationException a) {
                } catch (final Exception exception) {
                    connException.setValue(exception);
                }
                return null;
            }
        }, null, null);

        if (connException.getValue() == Boolean.TRUE) {
            Messages.showInfoMessage(project, PluginBundle.message(PluginBundle.LOGIN_SUCCESS_MESSAGE),
                    PluginBundle.message(PluginBundle.CONNECTION_STATUS_TITLE));
        } else if (connException.getValue() instanceof Exception) {
            final ExceptionHandler.Message message = ExceptionHandler.getMessage((Exception) connException.getValue());
            Messages.showErrorDialog(message.message,
                    PluginBundle.message(PluginBundle.CONNECTION_STATUS_TITLE));
        }
    }

    private void fetchCurrentConfiguration() {
        boolean useToken = loginPanel.useToken();
        currentConfigurationState.url = loginPanel.getUrl();
        currentConfigurationState.username = useToken ? null : loginPanel.getUsername();
        currentConfigurationState.password = useToken ? null : loginPanel.getPassword();
        currentConfigurationState.token = useToken ? loginPanel.getToken() : null;
        currentConfigurationState.useToken = useToken;
        currentConfigurationState.useRbTools = loginPanel.useRbTools();
        currentConfigurationState.rbtPath = loginPanel.rbtPath();
    }
}

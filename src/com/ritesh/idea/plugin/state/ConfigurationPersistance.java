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

import com.intellij.openapi.components.*;
import com.intellij.openapi.project.Project;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.Nullable;

/**
 * @author Ritesh
 */
@State(
        name = "reviewBoardPlugin",
        reloadable = true,
        storages = {
                @Storage(id = "default", file = "$PROJECT_FILE$"),
                @Storage(id = "dir", file = "$PROJECT_CONFIG_DIR$/review_board_plugin_settings_v0_1.xml",
                        scheme = StorageScheme.DIRECTORY_BASED)
        }
)
public class ConfigurationPersistance implements PersistentStateComponent<Configuration> {

    private ConfigurationPersistance() {
    }

    @Nullable
    public static ConfigurationPersistance getInstance(Project project) {
        return ServiceManager.getService(project, ConfigurationPersistance.class);
    }

    private final Configuration state = new Configuration();

    @Nullable
    @Override
    public Configuration getState() {
        return state;
    }

    @Override
    public void loadState(Configuration state) {
        XmlSerializerUtil.copyBean(state, this.state);
        this.state.loadExternal();
    }

    public void saveState(Configuration state) {
        this.state.clearExternal();
        state.saveExternal();
        XmlSerializerUtil.copyBean(state, this.state);
        this.state.loadExternal();
    }
}

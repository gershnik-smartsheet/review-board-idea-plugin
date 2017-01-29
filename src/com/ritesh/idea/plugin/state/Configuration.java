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

import com.intellij.ide.passwordSafe.PasswordSafe;
import com.intellij.ide.passwordSafe.PasswordSafeException;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.util.xmlb.annotations.Transient;
import com.ritesh.idea.plugin.exception.InvalidConfigurationException;
import com.ritesh.idea.plugin.reviewboard.Credentials;
import org.apache.commons.lang.StringUtils;

/**
 * @author Ritesh
 */
public class Configuration implements Cloneable {

    public static final Logger LOG = Logger.getInstance(Configuration.class);

    public String url;
    public String username;
    @Transient
    public String password;
    @Transient
    public String token;
    public Boolean useToken;
    public Boolean useRbTools;
    public String rbtPath;

    public Configuration(String url,
                         String username,
                         String password,
                         String token,
                         Boolean useToken,
                         Boolean useRbTools,
                         String rbtPath) {
        this.url = url;
        this.username = username;
        this.password = password;
        this.token = token;
        this.useToken = useToken;
        this.useRbTools = useRbTools;
        this.rbtPath = rbtPath;
    }

    public Configuration() {
    }

    public Configuration(Configuration src) {
        this(src.url, src.username, src.password, src.token, src.useToken, src.useRbTools, src.rbtPath);
    }

    @Override
    public String toString() {
        return "Configuration{" +
                "username='" + username + '\'' +
                ", url='" + url + '\'' +
                ", useRbTools='" + useRbTools + '\'' +
                ", rbtPath='" + rbtPath + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Configuration that = (Configuration) o;

        return !(url != null ? !url.equals(that.url) : that.url != null)
                && !(username != null ? !username.equals(that.username) : that.username != null)
                && !(password != null ? !password.equals(that.password) : that.password != null)
                && !(token != null ? !token.equals(that.token) : that.token != null)
                && !(useToken != null ? !useToken.equals(that.useToken) : that.useToken != null)
                && !(rbtPath != null ? !rbtPath.equals(that.rbtPath) : that.rbtPath != null)
                && !(useRbTools != null ? !useRbTools.equals(that.useRbTools) : that.useRbTools != null);

    }

    @Override
    public int hashCode() {
        int result = url != null ? url.hashCode() : 0;
        result = 31 * result + (username != null ? username.hashCode() : 0);
        result = 31 * result + (password != null ? password.hashCode() : 0);
        result = 31 * result + (token != null ? token.hashCode() : 0);
        result = 31 * result + (useToken != null ? useToken.hashCode() : 0);
        result = 31 * result + (useRbTools != null ? useRbTools.hashCode() : 0);
        result = 31 * result + (rbtPath != null ? rbtPath.hashCode() : 0);
        return result;
    }

    public Credentials createCredentials() {

        if (useToken == null || !useToken) {

            if (StringUtils.isEmpty(username) || StringUtils.isEmpty(password))
                throw new InvalidConfigurationException("Review board not configured properly");

            return new Credentials.UsernamePassword(username, password);

        } else {

            if (StringUtils.isEmpty(token))
                throw new InvalidConfigurationException("Review board not configured properly");

            return new Credentials.ApiToken(token);
        }
    }

    public boolean isValid() {
        if (StringUtils.isEmpty(url))
            return false;

        if (useToken == null || !useToken) {

            if (StringUtils.isEmpty(username) || StringUtils.isEmpty(password))
                return false;

        } else {

            if (StringUtils.isEmpty(token))
                return false;

        }

        return true;
    }

    public void loadExternal() {
        if (url == null)
            return;

        try {
            String key = "RB token for " + url;
            token = PasswordSafe.getInstance().getPassword(null, getClass(), key);
        } catch (PasswordSafeException e) {
            LOG.info("Couldn't get API token", e);
        }

        if (username == null)
            return;

        String key = username + " at " + url;
        try {
            password = PasswordSafe.getInstance().getPassword(null, getClass(), key);
        } catch (PasswordSafeException e) {
            LOG.info("Couldn't get password for user " + username, e);
        }

    }

    public void saveExternal() {
        if (url == null)
            return;

        try {
            String key = "RB token for " + url;
            if (token != null)
                PasswordSafe.getInstance().storePassword(null, getClass(), key, token);
            else
                PasswordSafe.getInstance().removePassword(null, getClass(), key);
        } catch (PasswordSafeException e) {
            LOG.info("Couldn't set store API token", e);
        }

        if (username == null)
            return;

        try {
            String key = username + " at " + url;
            if (password != null)
                PasswordSafe.getInstance().storePassword(null, getClass(), key, password);
            else
                PasswordSafe.getInstance().removePassword(null, getClass(), key);
        } catch (PasswordSafeException e) {
            LOG.info("Couldn't set password for user " + username, e);
        }
    }

    public void clearExternal() {
        if (url == null)
            return;

        try {
            String key = "RB token for " + url;
            PasswordSafe.getInstance().removePassword(null, getClass(), key);
        } catch (PasswordSafeException e) {
            LOG.info("Couldn't set store API token", e);
        }

        if (username == null)
            return;

        try {
            String key = username + " at " + url;
            PasswordSafe.getInstance().removePassword(null, getClass(), key);
        } catch (PasswordSafeException e) {
            LOG.info("Couldn't set password for user " + username, e);
        }
    }

}

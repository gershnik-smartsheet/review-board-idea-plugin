package com.ritesh.idea.plugin.reviewboard;

import org.apache.commons.codec.binary.Base64;

import java.util.Arrays;
import java.util.List;

public interface Credentials {

    String getAuthorizationHeader();
    List<String> getRbtCommandLine();

    final class UsernamePassword implements Credentials {

        private final String userName;
        private final String password;

        public UsernamePassword(String userName, String password) {
            this.userName = userName;
            this.password = password;
        }

        @Override
        public String getAuthorizationHeader() {
            return "Basic " + Base64.encodeBase64String((userName + ":" + password).getBytes());
        }

        @Override
        public List<String> getRbtCommandLine() {
            return Arrays.asList("--username", userName, "--password", password);
        }
    }

    final class ApiToken implements Credentials {

        private final String token;

        public ApiToken(String token) {
            this.token = token;
        }

        @Override
        public String getAuthorizationHeader() {
            return "token " + token;
        }

        @Override
        public List<String> getRbtCommandLine() {
            return Arrays.asList("--api-token", token);
        }
    }
}

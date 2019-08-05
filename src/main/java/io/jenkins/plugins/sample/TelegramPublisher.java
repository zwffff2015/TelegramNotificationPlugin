package io.jenkins.plugins.sample;

import hudson.Extension;
import hudson.Launcher;
import hudson.model.*;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Notifier;
import hudson.tasks.Publisher;
import hudson.util.FormValidation;
import net.sf.json.JSONObject;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

import java.io.IOException;
import java.util.HashSet;

public class TelegramPublisher extends Notifier {
    private final String receivers;
    private final String messageTemplate;
    private final Boolean condition;

    @DataBoundConstructor
    public TelegramPublisher(String receivers, String messageTemplate, Boolean condition) {
        this.receivers = receivers;
        this.messageTemplate = messageTemplate;
        this.condition = condition;
    }

    public String getMessageTemplate() {
        return messageTemplate;
    }

    public String getReceivers() {
        return receivers;
    }

    public Boolean getCondition() {
        return condition;
    }

    @Override
    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl) super.getDescriptor();
    }

    @Override
    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) throws InterruptedException, IOException {
        Result buildResult = build.getResult();
        String message = GetMessage(build);

        if (getDescriptor().isShowLog())
            listener.getLogger().println(message);

        TelegramMessageBot bot = new TelegramMessageBot(getDescriptor().getBotUserName(), getDescriptor().getBotToken());

        if (!buildResult.equals(Result.SUCCESS)) {
            String[] globalNotifyUsersArr = getDescriptor().getGlobalNotifyUsers().isEmpty() ? new String[]{} : getDescriptor().getGlobalNotifyUsers().split("#");
            HashSet<String> globalNotifyUsers = new HashSet<String>();
            for (String user : globalNotifyUsersArr) {
                if (!globalNotifyUsers.contains(user))
                    globalNotifyUsers.add(user);
            }
            for (String user : globalNotifyUsers) {
                bot.SendMessage(Long.parseLong(user), message);
            }

            String[] receivedUsers = this.getReceivers().isEmpty() ? new String[]{} : this.getReceivers().split("#");
            for (String user : receivedUsers) {
                bot.SendMessage(Long.parseLong(user), message);
            }
            return false;
        }

        if (getCondition()) {
            String[] globalNotifyUsersArr = getDescriptor().getGlobalNotifyUsers().isEmpty() ? new String[]{} : getDescriptor().getGlobalNotifyUsers().split("#");
            HashSet<String> globalNotifyUsers = new HashSet<String>();
            for (String user : globalNotifyUsersArr) {
                if (!globalNotifyUsers.contains(user))
                    globalNotifyUsers.add(user);
            }
            for (String user : globalNotifyUsers) {
                bot.SendMessage(Long.parseLong(user), message);
            }

            String[] receivedUsers = this.getReceivers().isEmpty() ? new String[]{} : this.getReceivers().split("#");
            for (String user : receivedUsers) {
                bot.SendMessage(Long.parseLong(user), message);
            }
        }
        return true;
    }

    private String GetMessage(AbstractBuild<?, ?> build) {
        if (getMessageTemplate().isEmpty())
            return "Project " + build.getFullDisplayName() + " has finished build. Build status is " + build.getResult().toString();

        return getMessageTemplate() + ", Project " + build.getFullDisplayName() + " has finished build. Build status is " + build.getResult().toString();
    }

    @Symbol("Telegram")
    @Extension
    public static final class DescriptorImpl extends BuildStepDescriptor<Publisher> {
        private String botUserName;
        private String botToken;
        private boolean showLog;
        private String globalNotifyUsers;

        public String getBotUserName() {
            return botUserName;
        }

        public String getBotToken() {
            return botToken;
        }

        public boolean isShowLog() {
            return showLog;
        }

        public String getGlobalNotifyUsers() {
            return globalNotifyUsers;
        }

        public DescriptorImpl() {
            load();
        }

        ///校验botUserName参数
        public FormValidation doCheckBotUserName(@QueryParameter String value) {
            if (value.length() == 0)
                return FormValidation.error(Messages.TelegramPublisher_DescriptorImpl_errors_missingBotUserName());

            return FormValidation.ok();
        }

        ///校验botToken参数
        public FormValidation doCheckBotToken(@QueryParameter String value) {
            if (value.length() == 0)
                return FormValidation.error(Messages.TelegramPublisher_DescriptorImpl_errors_missingBotToken());

            return FormValidation.ok();
        }

        @Override
        public boolean configure(StaplerRequest req, JSONObject json) throws FormException {
            botUserName = json.getString("botUserName");
            botToken = json.getString("botToken");
            showLog = json.getBoolean("showLog");
            globalNotifyUsers = json.getString("globalNotifyUsers");
            save();
            return super.configure(req, json);
        }

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            if (getBotUserName() == null || getBotUserName().isEmpty())
                return false;

            if (getBotToken() == null || getBotToken().isEmpty())
                return false;

            return true;
        }

        @Override
        public String getDisplayName() {
            return Messages.TelegramPublisher_DescriptorImpl_DisplayName();
        }
    }
}

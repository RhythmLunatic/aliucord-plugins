package com.accelerator.plugins;

import android.content.Context;
import android.os.Bundle;
import android.view.View;

import com.aliucord.annotations.AliucordPlugin;
import com.aliucord.api.SettingsAPI;
import com.aliucord.entities.MessageEmbedBuilder;
import com.aliucord.entities.Plugin;
import com.aliucord.patcher.Hook;
import com.aliucord.widgets.BottomSheet;
import com.aliucord.wrappers.embeds.MessageEmbedWrapper;
import com.aliucord.Logger;
import com.aliucord.utils.ReflectUtils;

import com.discord.api.channel.Channel;
import com.discord.api.message.embed.EmbedType;
import com.discord.models.member.GuildMember;
import com.discord.models.message.Message;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;

import com.discord.api.message.embed.MessageEmbed;
import com.discord.stores.StoreMessageState;
import com.discord.views.CheckedSetting;
import com.discord.widgets.chat.list.entries.ChatListEntry;
import com.discord.widgets.chat.list.entries.EmbedEntry;

import android.view.View;

import com.discord.stores.StoreStream;
import com.discord.widgets.chat.list.adapter.WidgetChatListAdapterItemMessage;

import com.aliucord.Http;
import com.aliucord.Utils;
import com.aliucord.wrappers.embeds.VideoWrapper;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.discord.views.CheckedSetting;
import com.aliucord.api.SettingsAPI;
import com.aliucord.widgets.BottomSheet;
import android.os.Bundle;

import org.json.JSONArray;
import org.json.JSONObject;

// This class is never used so your IDE will likely complain. Let's make it shut up!
@SuppressWarnings("unused")
@AliucordPlugin
public class Main extends Plugin {

	public Main() {
        settingsTab = new SettingsTab(PluginSettings.class, SettingsTab.Type.BOTTOM_SHEET).withArgs(settings);
    }

    public static class PluginSettings extends BottomSheet {
        private final SettingsAPI settings;

        public PluginSettings(SettingsAPI settings) {
            this.settings = settings;
        }

        public void onViewCreated(View view, Bundle bundle) {
            super.onViewCreated(view, bundle);
            addView(createCheckedSetting(view.getContext(),  "VideoEmbeds", true, "Enable video embeds", null));
            addView(createCheckedSetting(view.getContext(),  "EnableProxy", false, "Enable proxy", "For example, if twitter is blocked in your country"));
        }

        private View createCheckedSetting(Context ctx, String key, boolean defVal, String title, String s) {
            CheckedSetting cs = Utils.createCheckedSetting(ctx, CheckedSetting.ViewType.SWITCH, title, s);
            cs.setChecked(settings.getBool(key, defVal));
            cs.setOnCheckedListener(check -> settings.setBool(key, check));
            return cs;
        }
    }

    public void start(Context context) {
        try {
            patcher.patch(
                    ChatListEntry.Companion.class.getDeclaredMethod(
                            "createEmbedEntries",
                            Message.class, //0
                            StoreMessageState.State.class, //1
                            boolean.class, //2
                            boolean.class, //3
                            boolean.class, //4
                            boolean.class, //5
                            boolean.class, //6
                            Channel.class, //7
                            GuildMember.class, //8
                            Map.class, //9
                            Map.class //10
                    ), new Hook(callFrame -> {
                        Message message = (Message)callFrame.args[0];

                        List<EmbedEntry> embedAndAttachmentList = (List)callFrame.getResult();
                        List<MessageEmbed> embeds = message.getEmbeds();

                        if (embedAndAttachmentList.size() < 1 || embeds.size() < 2 || embedAndAttachmentList.size() == embeds.size()) return;
                        long guildID = embedAndAttachmentList.get(0).getGuildId();

                        EmbedEntry lastAddedEmbed = (EmbedEntry)embedAndAttachmentList.get(embedAndAttachmentList.size()-1);
                        int embedIndex = lastAddedEmbed.component1()+1;

                        var embedColor = embeds.get(0).b() != null ? embeds.get(0).b() : null;
                        for (int i = 1; i < embeds.size(); i++) {
                            var embed = embeds.get(i);
                            if (embedColor != null) {
                                try {
                                    ReflectUtils.setField(embed,"color",embedColor);
                                } catch (Exception ignored) {}
                            }

                            embedAndAttachmentList.add(new EmbedEntry(
                                    embedIndex,
                                    guildID,
                                    message,
                                    (StoreMessageState.State)callFrame.args[1],
                                    embed,
                                    (boolean)callFrame.args[2],
                                    (boolean)callFrame.args[3],
                                    (boolean)callFrame.args[4],
                                    (boolean)callFrame.args[5]
                            ));
                            embedIndex++;
                        }
                    })
            );
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (settings.getBool("VideoEmbeds", true)) {
            try {
                patcher.patch(
                        WidgetChatListAdapterItemMessage.class.getDeclaredMethod("configureItemTag",
                                Message.class,
                                boolean.class
                        ), new Hook((cf) -> {
                            var msg = (Message) cf.args[0];
                            var embeds = msg.getEmbeds();
                            if (embeds.size() != 1) return;

                            MessageEmbedWrapper twEmbed = new MessageEmbedWrapper(embeds.get(0));
                            VideoWrapper v = twEmbed.getVideo();
                            if (v != null && v.getUrl().startsWith("https://twitter.com/i/videos")) {
                                Pattern pattern = Pattern.compile("\\/tweet\\/([0-9]*)");
                                Matcher matcher = pattern.matcher(v.getUrl());
                                if(matcher.find()){
                                    new Thread(() -> {
                                        try {
                                            //Note: p91878l7.beget.tech/api.php - https://gist.github.com/ServOKio/0c08858a7400b2d759fb3fa216926fc6
                                            //Why ? Directly twitter returned "Connection reset" #RussianSanctionsAgainstTwitter
                                            String defUrl = "https://cdn.syndication.twimg.com/tweet-result?lang=en&id="+matcher.group(1);
                                            URL obj = new URL(settings.getBool("EnableProxy", false) ? addParameter(addParameter("http://p91878l7.beget.tech/api.php", "reg", "get_content"), "url", defUrl) : defUrl);
                                            HttpURLConnection con = (HttpURLConnection) obj.openConnection();

                                            con.setRequestMethod("GET");

                                            con.setRequestProperty("cache-control", "max-age=0");
                                            con.setRequestProperty("upgrade-insecure-requests", "1");
                                            con.setRequestProperty("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:102.0) Gecko/20100101 Firefox/102.0");
                                            con.setRequestProperty("sec-fetch-user:", "?1");
                                            con.setRequestProperty("accept-language:", "ru-RU,ru;q=0.9,en-US;q=0.8,en;q=0.7");

                                            con.setRequestProperty("Accept-Charset", "UTF-8");
                                            con.setDoOutput(true);
                                            int responseCode = con.getResponseCode();
                                            if(responseCode == 200) {
                                                BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                                                String line;
                                                StringBuilder response = new StringBuilder();

                                                while ((line = in.readLine()) != null) response.append(line);
                                                in.close();

                                                String txt = response.toString();
                                                JSONObject json = new JSONObject(txt);

                                                MessageEmbedBuilder embed = new MessageEmbedBuilder(EmbedType.VIDEO);
                                                String poster = null;
                                                String videoURL = null;
                                                if (json.has("video")) {
                                                    if (json.getJSONObject("video").has("poster")) poster = json.getJSONObject("video").getString("poster");
                                                    JSONArray a = json.getJSONObject("video").getJSONArray("variants");
                                                    for (int i = 0; i < a.length(); i++) {
                                                        JSONObject va = a.getJSONObject(i);
                                                        if(va.getString("type").equals("video/mp4")) videoURL = va.getString("src");
                                                    }
                                                } else logger.debug(txt);

                                                if (videoURL != null || poster != null) {
                                                    if (videoURL == null) {
                                                        embed.setImage(poster);
                                                    } else embed.setVideo(videoURL);
                                                    msg.getEmbeds().add(embed.build());
                                                    StoreStream.getMessages().handleMessageUpdate(msg.synthesizeApiMessage());
                                                }
                                            }
                                        } catch (Exception e) {
                                            Utils.showToast("Failed to fix twitter video embed, message:\n"+e.getMessage());
                                            e.printStackTrace();
                                        }
                                    }).start();
                                }
                            }
                        })
                );
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private String addParameter(String URL, String name, String value) {
        int qpos = URL.indexOf('?');
        int hpos = URL.indexOf('#');
        char sep = qpos == -1 ? '?' : '&';
        String seg = sep + encodeUrl(name) + '=' + encodeUrl(value);
        return hpos == -1 ? URL + seg : URL.substring(0, hpos) + seg
                + URL.substring(hpos);
    }

    private String encodeUrl(String url) {
        try {
            return URLEncoder.encode(url, "UTF-8");
        } catch (UnsupportedEncodingException uee) {
            throw new IllegalArgumentException(uee);
        }
    }

    @Override
    public void stop(Context context) {
        patcher.unpatchAll();
    }
}

/*
Copyright (C) Rhythm Lunatic 2021

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <https://www.gnu.org/licenses/>.
*/

package com.beat.play.util;

import com.beat.play.model.Channel;

import java.util.ArrayList;
import java.util.List;

public class PlaylistParser {

    private PlaylistParser() {
    }

    public static List<Channel> parse(String content) {
        List<Channel> channels = new ArrayList<>();
        if (content == null) {
            return channels;
        }

        String name = null;
        String logo = null;
        String category = null;

        for (String rawLine : content.split("\n")) {
            String line = rawLine.trim();
            if (line.isEmpty()) {
                continue;
            }

            if (line.startsWith("#EXTINF")) {
                logo = extractAttribute(line, "tvg-logo");
                category = extractAttribute(line, "group-title");
                int idx = line.lastIndexOf(',');
                name = (idx >= 0 && idx < line.length() - 1) ? line.substring(idx + 1).trim() : "চ্যানেল";
            } else if (!line.startsWith("#") && line.startsWith("http")) {
                if (name != null && !name.isEmpty()) {
                    channels.add(new Channel(null, name, line, logo, category));
                }
                name = null;
                logo = null;
                category = null;
            }
        }
        return channels;
    }

    private static String extractAttribute(String line, String key) {
        String token = key + "=\"";
        int start = line.indexOf(token);
        if (start >= 0) {
            start += token.length();
            int end = line.indexOf('"', start);
            if (end > start) {
                return line.substring(start, end);
            }
        }
        return null;
    }
}

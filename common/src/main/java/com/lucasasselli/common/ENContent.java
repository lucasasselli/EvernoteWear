package com.lucasasselli.common;


public class ENContent {
    public static String getNoteContent(String raw){
        return getStringBetween(raw, "<en-note>", "</en-note>");
    }

    public static String noteCreateBody(String raw){
        String noteBody = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
        noteBody += "<!DOCTYPE en-note SYSTEM \"http://xml.evernote.com/pub/enml2.dtd\">";
        noteBody += "<en-note>" + raw + "</en-note>";

        return noteBody;
    }

    private static String getStringBetween(String raw, String start, String stop){
        return raw.substring(raw.indexOf(start)+start.length(), raw.indexOf(stop));
    }
}

package com.example.jyoti.myapplication;

import android.content.Context;
import android.util.Log;

import org.apache.http.conn.ConnectTimeoutException;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.SocketException;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by jyoti on 13/12/17.
 */


public class UploadService {

    HttpURLConnection httpUrlConnection = null;
    String boundary = "*****";
    //File imagePath ;
    PrintWriter writer;
    private String crlf = "\r\n";
    private String twoHyphens = "--";
    OutputStream outputStream;
    public static boolean isChunkedEncoded = true;

    public String startUploading(Context context, final File file_toupload) {

        try {

            if (context != null) {


                long fileLength = file_toupload.length() / 1024;
                long chunkLength = fileLength / 5;
                URL url = new URL(url_to_upload);
                httpUrlConnection = (HttpURLConnection) url.openConnection();
                httpUrlConnection.setUseCaches(false);
                httpUrlConnection.setDoOutput(true);
                httpUrlConnection.setDoInput(true);
                httpUrlConnection.setRequestProperty(
                        "Content-Type", "multipart/form-data; boundary=" + boundary);
                if (isChunkedEncoded)
                    httpUrlConnection.setChunkedStreamingMode((int) chunkLength);
                httpUrlConnection.setRequestProperty("Accept", "*/*");

                outputStream = httpUrlConnection.getOutputStream();
                writer = new PrintWriter(new OutputStreamWriter(outputStream, "UTF-8"),
                        true);


                addFormField("param1", "param1val");
                addFormField("param2", "param2val");

                addFilePart("image", file_toupload);

                writer.append(crlf);
                writer.append(twoHyphens + boundary + twoHyphens).append(crlf);
                writer.flush();


                outputStream.flush();
                outputStream.close();


                InputStream responseStream = new
                        BufferedInputStream(httpUrlConnection.getInputStream());

                BufferedReader responseStreamReader =
                        new BufferedReader(new InputStreamReader(responseStream));

                String line = "";
                StringBuilder stringBuilder = new StringBuilder();

                while ((line = responseStreamReader.readLine()) != null) {
                    stringBuilder.append(line).append("\n");
                }
                responseStreamReader.close();

                String response = stringBuilder.toString();
                responseStream.close();
                httpUrlConnection.disconnect();

                return response;

            }
        } catch (ConnectTimeoutException e) {
            //  e.printStackTrace();


        } catch (SocketException e) {
            //    e.printStackTrace();
            if (e.getMessage() != null && e.getMessage().contains("EPIPE")) {
                isChunkedEncoded = false;
                startUploading(context, file_toupload);

            }

        } catch (Exception e) {

            if (e.getMessage() != null && e.getMessage().contains("EPIPE")) {
                isChunkedEncoded = false;
                startUploading(context, file_toupload);

            }

        }

        return "failure";
    }

    private void addFormField(String name, String value) {
        String LINE_FEED = "\r\n";
        writer.append("--*****").append(LINE_FEED);
        writer.append("Content-Disposition: form-data; name=\"" + name + "\"")
                .append(LINE_FEED);
        writer.append("Content-Type: text/plain; charset=UTF-8").append(
                LINE_FEED);
        writer.append(LINE_FEED);
        writer.append(value).append(LINE_FEED);
        writer.flush();
        Log.e("LUI:writer1:", "aa==>" + writer);
    }

    private void addFilePart(String fieldName, File uploadFile)
            throws IOException {


        File file = uploadFile;
        FileInputStream fileInputStream = new FileInputStream(file);
        String fileName = uploadFile.getName();
        Filename myfile = new Filename(fileName, '/', '.');
        String file_extension = myfile.extension();
        String file_name = myfile.filename();
        file_name = file_name.replaceAll("[^a-zA-Z0-9]", "_");
        System.out.println(file_name + "." + file_extension);


        fileName = file_name + "." + file_extension;

        String LINE_FEED = "\r\n";
        writer.append("--*****").append(LINE_FEED);
        writer.append(
                "Content-Disposition: form-data; name=\"" + fieldName
                        + "\"; filename=\"" + fileName + "\"")
                .append(LINE_FEED);
        writer.append(
                "Content-Type: "
                        + URLConnection.guessContentTypeFromName(fileName))
                .append(LINE_FEED);
        writer.append("Content-Transfer-Encoding: binary").append(LINE_FEED);
        writer.append(LINE_FEED);
        writer.flush();

        int progress = 0;
        int bytesRead = 0;
        byte buf[] = new byte[100];
        BufferedInputStream bufInput = new BufferedInputStream(
                new FileInputStream(file));
        int tempPercent = 0;
        while ((bytesRead = bufInput.read(buf)) != -1) {
            // write output
            outputStream.write(buf, 0, bytesRead);
            outputStream.flush();
            progress += bytesRead;
            //
        }
        fileInputStream.close();

        writer.append(LINE_FEED);
        writer.flush();
    }


    class Filename {
        private String fullPath;
        private char pathSeparator, extensionSeparator;

        public Filename(String str, char sep, char ext) {
            fullPath = str;
            pathSeparator = sep;
            extensionSeparator = ext;
        }

        public String extension() {
            int dot = fullPath.lastIndexOf(extensionSeparator);
            return fullPath.substring(dot + 1);
        }

        public String filename() { // gets filename without extension
            int dot = fullPath.lastIndexOf(extensionSeparator);
            int sep = fullPath.lastIndexOf(pathSeparator);
            return fullPath.substring(sep + 1, dot);
        }

        public String path() {
            int sep = fullPath.lastIndexOf(pathSeparator);
            return fullPath.substring(0, sep);
        }
    }

}

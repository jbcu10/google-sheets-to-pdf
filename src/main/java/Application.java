import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.BatchUpdateValuesRequest;
import com.google.api.services.sheets.v4.model.BatchUpdateValuesResponse;
import com.google.api.services.sheets.v4.model.ValueRange;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Application {
    private static final String APPLICATION_NAME = "Google Sheets API Java Quickstart";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final String TOKENS_DIRECTORY_PATH = "tokens";

    private static final List<String> SCOPES = Collections.singletonList(SheetsScopes.DRIVE);
    private static final String CREDENTIALS_FILE_PATH = "credentials.json";
    private static final String APPLICATION_NAME_DRIVE = "Google Drive API Java Quickstart";
    private static final JsonFactory JSON_FACTORY_DRIVE = JacksonFactory.getDefaultInstance();
    private static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws IOException {
        // Load client secrets.
        InputStream in = Application.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
                .setAccessType("offline")
                .build();
        return new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver()).authorize("user");
    }

    public static void main(String... args) throws IOException, GeneralSecurityException {
        // Build a new authorized API client service.
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        final String spreadsheetId = "1jRjGgtd2yjv5Bm9nAqR8UozZqEoCrbgzLS9wVs32TcU";
        final String range = "A2";
        Sheets service = new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                .setApplicationName(APPLICATION_NAME)
                .build();
        Drive serviceDrive = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY_DRIVE, getCredentials(HTTP_TRANSPORT))
                .setApplicationName(APPLICATION_NAME_DRIVE)
                .build();


        updateDocs(service, spreadsheetId,"RAW");
        downloadFile(serviceDrive);

    }



    
    private static void updateDocs(Sheets sheets,   String spreadsheetId, String valueInputOption) throws IOException {
        List<ValueRange> data = new ArrayList<>();
        for (int a=2;a<12;a++) {
            data.add(new ValueRange()
                    .setRange("A"+a)
                    .setValues(Arrays.asList(
                            Arrays.asList("TEST"+a, "TEST", "TEST", "TEST", "TEST", "TEST"))));
        }


        BatchUpdateValuesRequest batchBody = new BatchUpdateValuesRequest()
                .setValueInputOption(valueInputOption)
                .setData(data);

        BatchUpdateValuesResponse batchResult = sheets.spreadsheets().values()
                .batchUpdate(spreadsheetId, batchBody)
                .execute();
        System.out.println( "total rows updated: "+  batchResult.getResponses().size());
    }

    private static void downloadFile(Drive driveService) throws IOException {
        String fileId = "1jRjGgtd2yjv5Bm9nAqR8UozZqEoCrbgzLS9wVs32TcU";
        //change the mimeType and File Name
        InputStream inputStreamReader =driveService.files().export(fileId,"application/pdf").executeMediaAsInputStream();
        File targetFile = new File("test.pdf");
        FileUtils.copyInputStreamToFile(inputStreamReader, targetFile);

    }


}

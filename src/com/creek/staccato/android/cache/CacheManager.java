package com.creek.staccato.android.cache;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.Set;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import android.util.Log;

import com.creek.staccato.domain.group.Group;
import com.creek.staccato.domain.group.GroupKey;
import com.creek.staccato.domain.message.GenericMessageTransformer;
import com.creek.staccato.domain.message.InformationMessage;
import com.creek.staccato.domain.message.TransformException;
import com.creek.staccato.domain.message.generic.AddressedMessage;
import com.creek.staccato.domain.message.generic.GenericMessage;
import com.creek.staccato.domain.message.generic.MultipleGroupMessage;
import com.creek.staccato.domain.message.generic.Transformable;
import com.creek.staccato.domain.profile.Profile;
import com.creek.staccato.domain.repositorymessage.RepositoryGroup;
import com.creek.staccato.domain.repositorymessage.RepositoryProfile;
import com.creek.staccato.domain.util.JSONTransformer;
import com.creek.staccato.repository.email.AbstractRepository;

/**
 * 
 * @author Andrey Pereverzin
 * 
 */
public class CacheManager {
    private static final String TAG = CacheManager.class.getSimpleName();
    
    private static CacheManager instance = new CacheManager();
    private static final String STACCATO_PATH = "/sdcard/Android/staccato";
    private static final String STACCATO_CACHE_PATH = "/sdcard/Android/staccato/cache";
    private static final String STACCATO_PERSONAL_PATH = "/sdcard/Android/staccato/cache/personal";
    private static final String STACCATO_MYPROFILE = "MYPROFILE";
    private static final String STACCATO_INPUT_PATH = "/sdcard/Android/staccato/cache/input";
    private static final String STACCATO_REPOSITORY_MESSAGES_PATH = "/sdcard/Android/staccato/cache/repository";
    private static final String STACCATO_INFORMATION_MESSAGES_PATH = "/sdcard/Android/staccato/cache/information";
    private static final String STACCATO_REQUESTS_PATH = "/sdcard/Android/staccato/cache/requests";
    private static final String STACCATO_RESPONSES_PATH = "/sdcard/Android/staccato/cache/responses";
    private static final String STACCATO_OUTPUT_PATH = "/sdcard/Android/staccato/cache/output";
    private static final String STACCATO_GROUPS_PATH = "/sdcard/Android/staccato/cache/groups";

    private CacheManager() {
        mkdirIfNecessary(STACCATO_PATH);
        mkdirIfNecessary(STACCATO_CACHE_PATH);
        mkdirIfNecessary(STACCATO_PERSONAL_PATH);
        mkdirIfNecessary(STACCATO_INPUT_PATH);
        mkdirIfNecessary(STACCATO_REPOSITORY_MESSAGES_PATH);
        mkdirIfNecessary(STACCATO_INFORMATION_MESSAGES_PATH);
        mkdirIfNecessary(STACCATO_REQUESTS_PATH);
        mkdirIfNecessary(STACCATO_RESPONSES_PATH);
        mkdirIfNecessary(STACCATO_OUTPUT_PATH);
        mkdirIfNecessary(STACCATO_GROUPS_PATH);
    }

    public static CacheManager getInstance() {
        return instance;
    }

    public <T extends AddressedMessage> void writeMessagesToInputDir(Set<T> messages) throws IOException {
        Log.e(TAG, "------------------writeToInput: " + messages.size());
        writeToDir(STACCATO_INPUT_PATH, messages);
    }

    public Set<GenericMessage> readGenericMessagesFromInputDir() throws TransformException {
        Set<GenericMessage> messages = new HashSet<GenericMessage>();
        readTransformablesFromDir(STACCATO_INPUT_PATH, messages, true);
        return messages;
    }

    public <T extends MultipleGroupMessage> void writeMessagesToRepositoryMessagesDir(Set<T> messages) throws IOException {
        Log.e(TAG, "------------------writeMessagesToRepositoryMessagesDir: " + messages.size());
        writeToDir(STACCATO_REPOSITORY_MESSAGES_PATH, messages);
    }

    public void writeMessagesToInformationMessagesDir(Set<InformationMessage> messages) throws IOException {
        Log.e(TAG, "------------------writeMessagesToInformationMessagesDir: " + messages.size());
        writeToDir(STACCATO_INFORMATION_MESSAGES_PATH, messages);
    }

    public Set<InformationMessage> readMessagesFromInformationMessagesDir() throws TransformException {
        Set<InformationMessage> messages = new HashSet<InformationMessage>();
        readTransformablesFromDir(STACCATO_INFORMATION_MESSAGES_PATH, messages, true);
        return messages;
    }

    public <T extends AddressedMessage> void writeMessagesToRequestsDir(Set<T> messages) throws IOException {
        Log.e(TAG, "------------------writeMessagesToRequestsDir: " + messages.size());
        writeToDir(STACCATO_REQUESTS_PATH, messages);
    }

    public <T extends AddressedMessage> void writeMessagesToResponsesDir(Set<T> messages) throws IOException {
        Log.e(TAG, "------------------writeMessagesToResponsesDir: " + messages.size());
        writeToDir(STACCATO_RESPONSES_PATH, messages);
    }

    public void writeMessagesToOutput(Set<GenericMessage> messages) throws IOException {
        writeToDir(STACCATO_OUTPUT_PATH, messages);
    }

    public Set<GenericMessage> readMessagesFromOutput() throws TransformException {
        Set<GenericMessage> messages = new HashSet<GenericMessage>();
        readTransformablesFromDir(STACCATO_OUTPUT_PATH, messages, true);
        return messages;
    }

    public void writeGroupsToFiles(Set<Group> groups) throws IOException {
        Log.i(TAG, "writeToGroups");
        Set<FileData> fileData = new HashSet<FileData>();
        for (Group group : groups) {
            String fileName = keyToFileName(group.getGroupKey().toJSON().toString());
            fileData.add(new FileData(fileName, new RepositoryGroup(group, AbstractRepository.VERSION)));
        }
        File gr = new File(STACCATO_GROUPS_PATH);
        writeMessagesToFiles(gr, fileData);
    }

    public void addOrUpdateGroup(Group group) throws IOException {
        Log.i(TAG, "addOrUpdateGroup");
        String fileName = keyToFileName(group.getGroupKey().toJSON().toString());
        FileData fileData = new FileData(fileName, new RepositoryGroup(group, AbstractRepository.VERSION));
        File gr = new File(STACCATO_GROUPS_PATH);
        writeMessageToFile(gr, fileData);
        File out = new File(STACCATO_OUTPUT_PATH);
        writeMessageToFile(out, fileData);
    }

    public Set<Group> readGroupsFromGroupsDir() throws TransformException {
        Set<RepositoryGroup> messages = new HashSet<RepositoryGroup>();
        readTransformablesFromDir(STACCATO_GROUPS_PATH, messages);
        Set<Group> groups = new HashSet<Group>();
        for (RepositoryGroup message : messages) {
            groups.add(message.getData());
        }
        return groups;
    }

    public Group readGroupFromFile(GroupKey groupKey) throws TransformException {
        String fileName = keyToFileName(groupKey.toJSON().toString());
        File gr = new File(STACCATO_GROUPS_PATH);
        return (Group)readTransformableFromFile(gr, fileName);
    }

    public void writeMyProfileToFile(Profile myProfile) throws IOException {
        Log.i(TAG, "writeMyProfileToFile");
        if(myProfile != null) {
            FileData fileData = new FileData(STACCATO_MYPROFILE, new RepositoryProfile(myProfile, AbstractRepository.VERSION));
            File pe = new File(STACCATO_PERSONAL_PATH);
            writeMessageToFile(pe, fileData);
        }
    }

    public Profile readMyProfileFromFile() throws TransformException {
        Log.i(TAG, "readMyProfileFromFile");
        Set<RepositoryProfile> profiles = new HashSet<RepositoryProfile>();
        readTransformablesFromDir(STACCATO_PERSONAL_PATH, profiles);
        if(profiles.size() > 0) {
            return profiles.iterator().next().getData();
        }
        return null;
    }

    public void addOrUpdateMyProfile(Profile profile) throws IOException {
        Log.i(TAG, "addOrUpdateMyProfile");
        FileData fileData = new FileData(STACCATO_MYPROFILE, new RepositoryProfile(profile, AbstractRepository.VERSION));
        File pe = new File(STACCATO_PERSONAL_PATH);
        writeMessageToFile(pe, fileData);
        File out = new File(STACCATO_OUTPUT_PATH);
        writeMessageToFile(out, fileData);
    }

    public int countInformationMessages() throws IOException {
        File folder = new File(STACCATO_INFORMATION_MESSAGES_PATH);
        int count = folder.listFiles().length;
        Log.e(TAG, "------------------countInformationMessages: " + count);
        return count;
    }

    private File mkdirIfNecessary(String dirName) {
        File dir = new File(dirName);
        if (!dir.exists()) {
            dir.mkdir();
        }
        return dir;
    }

    private <T extends GenericMessage> void writeToDir(String dirName, Set<T> messages) throws IOException {
        int i = 0;
        File dir = new File(dirName);
        for (GenericMessage message : messages) {
            //File f = File.createTempFile("sta", "ato", folder);
            File f = new File(dir, "sta" + System.currentTimeMillis() + i + "ato.tmp");
            f.createNewFile();
            Log.e(TAG, "------------------writeToDir: " + f.getAbsolutePath());
            OutputStream os = new FileOutputStream(f);
            os.write(message.toJSON().toString().getBytes());
            os.close();
            i++;
        }
    }

    private void writeMessagesToFiles(File folder, Set<FileData> messages) throws IOException {
        for (FileData message : messages) {
            writeMessageToFile(folder, message);
        }
    }

    private void writeMessageToFile(File folder, FileData message) throws IOException {
        File f = new File(folder, message.getFileName());
        f.createNewFile();
        OutputStream os = new FileOutputStream(f);
        os.write(message.getData().toJSON().toString().getBytes());
        os.close();
    }

    private <T extends Transformable> void readTransformablesFromDir(String dirName, Set<T> res) throws TransformException {
        readTransformablesFromDir(dirName, res, false);
    }

    private <T extends Transformable> void readTransformablesFromDir(String dirName, Set<T> res, boolean del) throws TransformException {
        File dir = new File(dirName);
        File[] files = dir.listFiles();
        for (File f : files) {
            res.add((T) readTransformableFromFile(f));
            if (del) {
                f.delete();
            }
        }
    }

    private <T extends Transformable> T readTransformableFromFile(File folder, String fileName) throws TransformException {
        File f = new File(folder, fileName);
        if (!f.exists()) {
            return null;
        }
        return readTransformableFromFile(f);
    }

    private <T extends Transformable> T readTransformableFromFile(File f) throws TransformException {
        try {
            BufferedReader in = new BufferedReader(new FileReader(f));
            String s = in.readLine();
            in.close();
            JSONParser parser = new JSONParser();
            JSONTransformer transformer = new JSONTransformer();
            parser.parse(s, transformer);
            JSONObject jsonObject = (JSONObject) transformer.getResult();
            return (T) GenericMessageTransformer.transform(jsonObject);
        } catch (IOException ex) {
            throw new TransformException(ex);
        } catch (ParseException ex) {
            throw new TransformException(ex);
        }
    }
    
    private String keyToFileName(String key) {
        return (key.replaceAll("[\\s\\{\\}\\[\\]\\*@\"\':\\.,]", "_"));
    }

    class FileData {
        String fileName;
        GenericMessage data;

        FileData(String fileName, GenericMessage data) {
            this.fileName = fileName;
            this.data = data;
        }

        public String getFileName() {
            return fileName;
        }

        public GenericMessage getData() {
            return data;
        }
    }
}

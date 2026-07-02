// android.content.Context — needed to reach system services like the ContentResolver
import android.content.Context;

// android.content.ContentResolver / ContentValues — how you talk to MediaStore
import android.content.ContentResolver;
import android.content.ContentValues;

// android.net.Uri — Android's reference to a piece of content (not a raw file path)
import android.net.Uri;

// android.os.Environment — gives constants like DIRECTORY_MUSIC
import android.os.Environment;

// android.provider.MediaStore — the API that manages shared media (music, photos, video)
import android.provider.MediaStore;

// android.webkit.MimeTypeMap — converts a file extension (mp3, m4a) into a MIME type
import android.webkit.MimeTypeMap;

// java.io.* — plain Java classes for reading/writing files
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public static void moveMusicToMediaStore(Context context) throws IOException {

    // the cached song file — could be .mp3, .m4a, .wav, whatever the app downloaded
    File source = new File("/data/data/com.echoai.musicapp/cache/music/song.mp3");

    // pull out just the file name, e.g. "song.mp3"
    String fileName = source.getName();

    // pull out just the extension, e.g. "mp3"
    String extension = fileName.substring(fileName.lastIndexOf('.') + 1);

    // turn "mp3" into "audio/mpeg", "m4a" into "audio/mp4", etc. automatically
    String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);

    // describe the new entry we want MediaStore to create in the Music folder
    ContentValues values = new ContentValues();
    values.put(MediaStore.Audio.Media.DISPLAY_NAME, fileName);              // keep original name
    values.put(MediaStore.Audio.Media.MIME_TYPE, mimeType);                  // correct type, any format
    values.put(MediaStore.Audio.Media.RELATIVE_PATH, Environment.DIRECTORY_MUSIC); // target = Music/

    // ContentResolver is how the app sends/receives data through MediaStore
    ContentResolver resolver = context.getContentResolver();

    // ask MediaStore to create the entry — it gives back a Uri pointing to it
    Uri uri = resolver.insert(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, values);

    // open a stream to read the cached file's bytes
    InputStream in = new FileInputStream(source);

    // open a stream to write into the new MediaStore entry (via its Uri)
    OutputStream out = resolver.openOutputStream(uri);

    // copy every byte from the cached file into the Music folder file
    in.transferTo(out);

    in.close();
    out.close();

    // delete the cached copy now that it lives in Music/
    source.delete();
}
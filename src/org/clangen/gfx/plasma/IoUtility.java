package org.clangen.gfx.plasma;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;

import android.content.Context;

public class IoUtility {
    public static String rawResourceToString(Context context, int resourceId) {
        InputStream stream =
            context.getResources().openRawResource(resourceId);

        try {
            if (stream != null) {
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                byte[] buffer = new byte[4096];
                int read = 0;

                while ((read = stream.read(buffer)) > 0) {
                    out.write(buffer, 0, read);
                }

                return out.toString();
            }
        }
        catch (IOException ex) {
        }
        finally {
            close(stream);
        }

        return null;
    }

    public static void close(Closeable c) {
        if (c !=  null) {
            try {
                c.close();
            }
            catch (IOException ex) {
            }
        }
    }
}

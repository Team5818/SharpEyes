package org.rivierarobotics.sharpeyes;

import org.rivierarobotics.protos.Game;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public class BTHelper {

    /**
     * Instructs the client to receive the game encoded on the stream.
     * The game is encoded using {@link Game#writeDelimitedTo(OutputStream)}, and so
     * can be read with {@link Game#parseDelimitedFrom(InputStream)}.
     */
    public static final int COMMAND_RECEIVE_GAME = 1;


    // UUID5("SharpEyes")
    public static final UUID RFCOMM_ID = UUID.fromString("266d4474-6bf4-5d2c-ab57-7ce7815be24d");

}

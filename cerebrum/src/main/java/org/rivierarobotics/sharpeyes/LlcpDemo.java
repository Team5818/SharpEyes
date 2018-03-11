package org.rivierarobotics.sharpeyes;

import java.io.IOException;
import java.util.Collection;

import org.nfctools.NfcAdapter;
import org.nfctools.llcp.LlcpConnectionManager;
import org.nfctools.llcp.LlcpConnectionManagerFactory;
import org.nfctools.llcp.LlcpConstants;
import org.nfctools.llcp.LlcpOverNfcip;
import org.nfctools.ndef.Record;
import org.nfctools.ndefpush.NdefPushFinishListener;
import org.nfctools.ndefpush.NdefPushLlcpService;
import org.nfctools.scio.TerminalHandler;
import org.nfctools.scio.TerminalMode;
import org.nfctools.spi.acs.AcsTerminal;
import org.nfctools.utils.LoggingNdefListener;

public class LlcpDemo {

    private NdefPushLlcpService ndefPushLlcpService;
    private boolean initiatorMode;
    private LlcpOverNfcip llcpOverNfcip;

    public LlcpDemo(boolean initiatorMode) {
        this.initiatorMode = initiatorMode;
        ndefPushLlcpService = new NdefPushLlcpService(new LoggingNdefListener());
        llcpOverNfcip = new LlcpOverNfcip(new LlcpConnectionManagerFactory() {

            @Override
            protected void configureConnectionManager(LlcpConnectionManager connectionManager) {
                connectionManager.registerWellKnownServiceAccessPoint(LlcpConstants.COM_ANDROID_NPP,
                        ndefPushLlcpService);
            }
        });
    }

    public void addMessages(Collection<Record> ndefRecords, NdefPushFinishListener finishListener) {
        ndefPushLlcpService.addMessages(ndefRecords, finishListener);
    }

    public void runDemo() throws IOException {
        TerminalMode terminalMode = initiatorMode ? TerminalMode.INITIATOR : TerminalMode.TARGET;
        TerminalHandler handler = new TerminalHandler();
        handler.addTerminal(new AcsTerminal());
        NfcAdapter nfcAdapter = new NfcAdapter(handler.getAvailableTerminal(), terminalMode);
        nfcAdapter.setNfcipConnectionListener(llcpOverNfcip);
        nfcAdapter.startListening();
        System.out.println("Mode: " + terminalMode);
        System.out.println("Waiting for P2P, press ENTER to exit");
        System.in.read();
    }

    public static void main(String[] args) {
        try {
            LlcpDemo service = new LlcpDemo(true);
            service.runDemo();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
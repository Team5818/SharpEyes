package org.rivierarobotics.sharpeyes;

import java.nio.charset.StandardCharsets;
import java.util.List;

import javax.smartcardio.Card;
import javax.smartcardio.CardChannel;
import javax.smartcardio.CardTerminal;
import javax.smartcardio.CommandAPDU;
import javax.smartcardio.TerminalFactory;

import org.nfctools.spi.acs.Apdu;

import jnasmartcardio.Smartcardio;

public class TestNfc {

    public static void main(String[] args) throws Exception {
        TerminalFactory terminalFactory = TerminalFactory.getInstance("PC/SC", null, new Smartcardio());

        List<CardTerminal> terminals = terminalFactory.terminals().list();
        CardTerminal term = terminals.get(0);
        if (!term.waitForCardPresent(1000000)) {
            throw new RuntimeException("no u #1");
        }
        System.err.println("I gotz the card!");
        Card card = term.connect("*");
        System.err.println("Card iz " + new String(card.getATR().getBytes(), StandardCharsets.US_ASCII));
        CardChannel channel = card.openLogicalChannel();
        int chId = channel.getChannelNumber();
    }
}

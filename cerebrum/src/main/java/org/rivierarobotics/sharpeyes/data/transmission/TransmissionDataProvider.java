package org.rivierarobotics.sharpeyes.data.transmission;

import static com.google.common.base.Preconditions.checkState;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.rivierarobotics.protos.TeamMatch;
import org.rivierarobotics.protos.TransmitFrame;
import org.rivierarobotics.protos.TransmitFrame.KindCase;
import org.rivierarobotics.sharpeyes.data.DataProvider;

import com.google.common.collect.ImmutableList;

public abstract class TransmissionDataProvider implements DataProvider {

    @Override
    public CompletableFuture<List<TeamMatch>> provideMatches() {
        return getFrames().thenApply(frames -> {
            ImmutableList.Builder<TeamMatch> b = ImmutableList.builder();
            checkState(frames.hasNext(), "no frames!");
            TransmitFrame tf = frames.next();
            checkState(tf.getKindCase() == KindCase.START, "incorrect start message!");
            while (frames.hasNext()) {
                tf = frames.next();
                checkState(tf.getKindCase() != KindCase.START, "duplicate start message!");
                if (tf.getKindCase() == KindCase.END) {
                    break;
                }
                b.add(tf.getMatch());
            }
            checkState(tf.getKindCase() == KindCase.END, "improperly ended stream!");
            return b.build();
        });
    }

    protected abstract CompletableFuture<Iterator<TransmitFrame>> getFrames();

}

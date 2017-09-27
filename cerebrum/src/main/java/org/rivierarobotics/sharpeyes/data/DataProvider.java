package org.rivierarobotics.sharpeyes.data;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.rivierarobotics.protos.TeamMatch;

public interface DataProvider {

    CompletableFuture<List<TeamMatch>> provideMatches();

}

package de.memorian.ps4trophaen.tasks;

import java.util.ArrayList;
import java.util.List;

import de.memorian.ps4trophaen.DBSyncActivity;

/**
 *
 * @since 29.10.2014
 */
public class ProgressHandler implements TaskFinishedListener {

    private final List<GetTrophiesTask> trophiesTasks;
    private final DBSyncActivity dbSyncActivity;

    public ProgressHandler(DBSyncActivity activity, int taskAmount) {
        trophiesTasks = new ArrayList<GetTrophiesTask>(taskAmount);
        dbSyncActivity = activity;
        dbSyncActivity.setGamesAmount(taskAmount);
    }

    public void addTask(GetTrophiesTask task) {
        trophiesTasks.add(task);
        task.addListener(this);
    }

    @Override
    public void onTaskFinished(GetTrophiesTask getTrophiesTask) {
        trophiesTasks.remove(getTrophiesTask);
        dbSyncActivity.gameFinished();
        if (trophiesTasks.isEmpty()) {
            syncFinished();
        }
    }

    private void syncFinished() {
        dbSyncActivity.syncFinished();
    }
}

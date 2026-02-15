package com.example.bedwars.setup;

import com.example.bedwars.map.MapConfig;
import org.bukkit.Location;

public class SetupSession {
    private boolean awaitingMapName;
    private boolean awaitingMapRename;
    private boolean awaitingMapDeleteConfirm;
    private String targetMapName;
    private boolean awaitingNpcRename;
    private Integer targetNpcIndex;
    private String editingMapName;
    private com.example.bedwars.game.TeamColor editingTeam;
    private boolean awaitingBoundsPos1;
    private boolean awaitingBoundsPos2;
    private Location boundsPos1;
    private MapConfig currentMap;
    private boolean awaitingWorldEditConfirm;

    public boolean isAwaitingMapName() {
        return awaitingMapName;
    }

    public void setAwaitingMapName(boolean awaitingMapName) {
        this.awaitingMapName = awaitingMapName;
    }

    public boolean isAwaitingMapRename() {
        return awaitingMapRename;
    }

    public void setAwaitingMapRename(boolean awaitingMapRename) {
        this.awaitingMapRename = awaitingMapRename;
    }

    public boolean isAwaitingMapDeleteConfirm() {
        return awaitingMapDeleteConfirm;
    }

    public void setAwaitingMapDeleteConfirm(boolean awaitingMapDeleteConfirm) {
        this.awaitingMapDeleteConfirm = awaitingMapDeleteConfirm;
    }

    public String getTargetMapName() {
        return targetMapName;
    }

    public void setTargetMapName(String targetMapName) {
        this.targetMapName = targetMapName;
    }

    public boolean isAwaitingNpcRename() {
        return awaitingNpcRename;
    }

    public void setAwaitingNpcRename(boolean awaitingNpcRename) {
        this.awaitingNpcRename = awaitingNpcRename;
    }

    public Integer getTargetNpcIndex() {
        return targetNpcIndex;
    }

    public void setTargetNpcIndex(Integer targetNpcIndex) {
        this.targetNpcIndex = targetNpcIndex;
    }

    public String getEditingMapName() {
        return editingMapName;
    }

    public void setEditingMapName(String editingMapName) {
        this.editingMapName = editingMapName;
    }

    public com.example.bedwars.game.TeamColor getEditingTeam() {
        return editingTeam;
    }

    public void setEditingTeam(com.example.bedwars.game.TeamColor editingTeam) {
        this.editingTeam = editingTeam;
    }

    public boolean isAwaitingBoundsPos1() {
        return awaitingBoundsPos1;
    }

    public void setAwaitingBoundsPos1(boolean awaitingBoundsPos1) {
        this.awaitingBoundsPos1 = awaitingBoundsPos1;
    }

    public boolean isAwaitingBoundsPos2() {
        return awaitingBoundsPos2;
    }

    public void setAwaitingBoundsPos2(boolean awaitingBoundsPos2) {
        this.awaitingBoundsPos2 = awaitingBoundsPos2;
    }

    public Location getBoundsPos1() {
        return boundsPos1;
    }

    public void setBoundsPos1(Location boundsPos1) {
        this.boundsPos1 = boundsPos1;
    }

    public MapConfig getCurrentMap() {
        return currentMap;
    }

    public void setCurrentMap(MapConfig currentMap) {
        this.currentMap = currentMap;
    }

    public boolean isAwaitingWorldEditConfirm() {
        return awaitingWorldEditConfirm;
    }

    public void setAwaitingWorldEditConfirm(boolean awaitingWorldEditConfirm) {
        this.awaitingWorldEditConfirm = awaitingWorldEditConfirm;
    }
}

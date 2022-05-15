package me.swipez.betterplayerleads;

import org.bukkit.Bukkit;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.scoreboard.Team;

public class CollisionTeam {
    private Scoreboard board;

    private Team team;

    public CollisionTeam() {
        ScoreboardManager manager = Bukkit.getScoreboardManager();
        Scoreboard board = manager.getNewScoreboard();
        Team team = board.registerNewTeam("NoCollision");
        team.setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.NEVER);
        this.team = team;
        this.board = board;
    }

    public Team getTeam() {
        return this.team;
    }

    public Scoreboard getBoard() {
        return this.board;
    }
}

package com.skillexchange.model;

import java.util.List;

public class MatchResult {

    private User user;
    private List<String> theyCanTeachYou;
    private List<String> youCanTeachThem;
    private boolean mutualMatch;

    public MatchResult(User user, List<String> theyCanTeachYou, List<String> youCanTeachThem) {
        this.user = user;
        this.theyCanTeachYou = theyCanTeachYou;
        this.youCanTeachThem = youCanTeachThem;
        this.mutualMatch = !theyCanTeachYou.isEmpty() && !youCanTeachThem.isEmpty();
    }

    public User getUser() {
        return user;
    }

    public List<String> getTheyCanTeachYou() {
        return theyCanTeachYou;
    }

    public List<String> getYouCanTeachThem() {
        return youCanTeachThem;
    }

    public boolean isMutualMatch() {
        return mutualMatch;
    }
}

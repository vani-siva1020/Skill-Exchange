package com.skillexchange.service;

import com.skillexchange.model.MatchResult;
import com.skillexchange.model.User;
import com.skillexchange.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MatchService {

    private final UserRepository userRepository;

    public MatchService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Core real-time matching logic:
     * For the given user, find every other user where:
     *   - the other person can teach at least one skill this user WANTS, and/or
     *   - this user can teach at least one skill the other person WANTS.
     * Mutual matches (both directions satisfied) are ranked highest.
     */
    public List<MatchResult> findMatches(Long userId) {
        User currentUser = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));

        List<String> myWanted = normalize(currentUser.getSkillsWanted());
        List<String> myOffered = normalize(currentUser.getSkillsOffered());

        List<MatchResult> results = new ArrayList<>();

        for (User other : userRepository.findAll()) {
            if (other.getId().equals(currentUser.getId())) {
                continue;
            }

            List<String> theyCanTeachYou = intersect(normalize(other.getSkillsOffered()), myWanted);
            List<String> youCanTeachThem = intersect(myOffered, normalize(other.getSkillsWanted()));

            if (!theyCanTeachYou.isEmpty() || !youCanTeachThem.isEmpty()) {
                results.add(new MatchResult(other, theyCanTeachYou, youCanTeachThem));
            }
        }

        // Mutual (two-way) matches first, then by total overlap count
        results.sort(
                Comparator.comparing(MatchResult::isMutualMatch).reversed()
                        .thenComparing((MatchResult m) ->
                                -(m.getTheyCanTeachYou().size() + m.getYouCanTeachThem().size()))
        );

        return results;
    }

    private List<String> normalize(List<String> skills) {
        return skills.stream()
                .map(String::trim)
                .map(String::toLowerCase)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }

    private List<String> intersect(List<String> a, List<String> b) {
        return a.stream().filter(b::contains).distinct().collect(Collectors.toList());
    }
}

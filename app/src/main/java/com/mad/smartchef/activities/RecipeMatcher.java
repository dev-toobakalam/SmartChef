package com.mad.smartchef.activities;

import android.text.TextUtils;

import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.mad.smartchef.models.RecipeModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RecipeMatcher {

    public static List<RecipeModel> getMatchingRecipes(List<String> userIngredients, List<QueryDocumentSnapshot> docs) {
        List<ScoredRecipe> scoredRecipes = new ArrayList<>();

        for (QueryDocumentSnapshot doc : docs) {
            String id = doc.getId();
            String name = doc.getString("name");
            String imgurl = doc.getString("imgurl");
            String instructions = doc.getString("instructions");
            List<String> recipeIngredients = (List<String>) doc.get("ingredients");

            if (recipeIngredients == null || recipeIngredients.isEmpty()) continue;

            Set<String> lowerCaseIngredients = new HashSet<>();
            for (String ri : recipeIngredients) {
                lowerCaseIngredients.add(ri.toLowerCase().trim());
            }

            int matchScore = 0;
            for (String userInput : userIngredients) {
                for (String recipeIng : lowerCaseIngredients) {
                    if (getSimilarity(userInput, recipeIng) >= 0.7f) {
                        matchScore++;
                        break; // Avoid counting same ingredient multiple times
                    }
                }
            }

            if (matchScore > 0) {
                RecipeModel recipe = new RecipeModel(id, name, imgurl, instructions, new ArrayList<>(lowerCaseIngredients), matchScore);
                scoredRecipes.add(new ScoredRecipe(recipe, matchScore));
            }
        }

        // Sort by score (highest first), pick top 5
        Collections.sort(scoredRecipes, (r1, r2) -> Integer.compare(r2.score, r1.score));

        List<RecipeModel> finalList = new ArrayList<>();
        for (int i = 0; i < Math.min(5, scoredRecipes.size()); i++) {
            finalList.add(scoredRecipes.get(i).recipe);
        }

        return finalList;
    }

    // Levenshtein similarity: returns value between 0 and 1
    private static float getSimilarity(String s1, String s2) {
        if (TextUtils.isEmpty(s1) || TextUtils.isEmpty(s2)) return 0f;

        s1 = s1.toLowerCase().trim();
        s2 = s2.toLowerCase().trim();

        int maxLen = Math.max(s1.length(), s2.length());
        if (maxLen == 0) return 1f;

        int distance = levenshtein(s1, s2);
        return 1f - ((float) distance / maxLen);
    }

    private static int levenshtein(String a, String b) {
        int[] costs = new int[b.length() + 1];
        for (int j = 0; j < costs.length; j++) costs[j] = j;

        for (int i = 1; i <= a.length(); i++) {
            costs[0] = i;
            int nw = i - 1;

            for (int j = 1; j <= b.length(); j++) {
                int cj = Math.min(1 + Math.min(costs[j], costs[j - 1]),
                        a.charAt(i - 1) == b.charAt(j - 1) ? nw : nw + 1);
                nw = costs[j];
                costs[j] = cj;
            }
        }

        return costs[b.length()];
    }

    static class ScoredRecipe {
        RecipeModel recipe;
        int score;

        ScoredRecipe(RecipeModel recipe, int score) {
            this.recipe = recipe;
            this.score = score;
        }
    }
}

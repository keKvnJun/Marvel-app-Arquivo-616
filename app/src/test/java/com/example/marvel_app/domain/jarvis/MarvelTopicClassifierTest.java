package com.example.marvel_app.domain.jarvis;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class MarvelTopicClassifierTest {
    private final MarvelTopicClassifier classifier = new MarvelTopicClassifier();

    @Test
    public void acceptsMarvelQuestionsWithAccentsAndPunctuation() {
        assertEquals(
                MarvelTopicClassifier.Result.MARVEL,
                classifier.classify("Quem é o Capitão América?")
        );
    }

    @Test
    public void rejectsQuestionOutsideMission() {
        assertEquals(
                MarvelTopicClassifier.Result.OUT_OF_SCOPE,
                classifier.classify("Qual é a previsão do tempo para amanhã?")
        );
        assertTrue(JarvisResponses.forClassification(MarvelTopicClassifier.Result.OUT_OF_SCOPE)
                .contains("fora da missão"));
    }

    @Test
    public void doesNotTreatGenericSuperheroQuestionAsMarvel() {
        assertEquals(
                MarvelTopicClassifier.Result.OUT_OF_SCOPE,
                classifier.classify("Qual é o poder do Superman?")
        );
    }

    @Test
    public void treatsBlankInputAsEmpty() {
        assertEquals(MarvelTopicClassifier.Result.EMPTY, classifier.classify("   "));
        assertEquals(MarvelTopicClassifier.Result.EMPTY, classifier.classify(null));
    }
}

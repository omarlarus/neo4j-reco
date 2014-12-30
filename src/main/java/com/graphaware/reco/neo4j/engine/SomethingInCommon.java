package com.graphaware.reco.neo4j.engine;

import com.graphaware.reco.generic.context.Context;
import com.graphaware.reco.generic.engine.SingleScoreRecommendationEngine;
import com.graphaware.reco.generic.transform.ScoreTransformer;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;

import java.util.HashMap;
import java.util.Map;

import static org.neo4j.graphdb.Direction.*;

/**
 * {@link SingleScoreRecommendationEngine} that recommends {@link Node}s with which have something in common. In other
 * words, there is a path of length two between the subject node (the input to the recommendation) and the recommended node.
 * <p/>
 * Moreover, both relationships of the path have the same type (specified by {@link #getType()} and unless {@link #getDirection()}
 * is {@link Direction#BOTH}, the first relationship of the path is of the specified direction and the second one if of
 * the opposite direction.
 * <p/>
 * Every time a recommendation is found, it's score is incremented by {@link #scoreNode(org.neo4j.graphdb.Node)}.
 */
public abstract class SomethingInCommon extends SingleScoreRecommendationEngine<Node, Node> {

    /**
     * Construct a recommendation engine that performs no score transformation.
     */
    public SomethingInCommon() {
        super();
    }

    /**
     * Construct a recommendation engine that transforms all scores using the provided transformer.
     *
     * @param transformer for scores, must not be <code>null</code>.
     */
    public SomethingInCommon(ScoreTransformer transformer) {
        super(transformer);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Map<Node, Integer> doRecommend(Node input, Context<Node, Node> context) {
        Map<Node, Integer> result = new HashMap<>();

        for (Relationship r1 : input.getRelationships(getType(), getDirection())) {
            Node thingInCommon = r1.getOtherNode(input);
            for (Relationship r2 : thingInCommon.getRelationships(getType(), reverse(getDirection()))) {
                Node node = r2.getOtherNode(thingInCommon);
                if (node.getId() != input.getId()) {
                    result.put(node, scoreNode(node));
                }
            }
        }

        return result;
    }

    /**
     * Score the recommended node.
     *
     * @param node to score.
     * @return score, 1 by default.
     */
    protected int scoreNode(Node node) {
        return 1;
    }


    /**
     * Get the relationship type of the relationship that links the subject of the recommendation and the recommended
     * item with the thing in common.
     *
     * @return relationship type.
     */
    protected abstract RelationshipType getType();

    /**
     * Get the direction of the relationship between the subject (input to the engine) and the thing in common.
     *
     * @return direction.
     */
    protected abstract Direction getDirection();

    /**
     * Reverse direction.
     *
     * @param direction to reverse.
     * @return reversed direction.
     */
    private Direction reverse(Direction direction) {
        switch (direction) {
            case BOTH:
                return BOTH;
            case OUTGOING:
                return INCOMING;
            case INCOMING:
                return OUTGOING;
            default:
                throw new IllegalArgumentException("Unknown direction " + direction);
        }
    }
}
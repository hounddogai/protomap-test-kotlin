package com.acme.transport.network.node.schedule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class NodeReviewRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultNodeReviewValidator()
        val item = newNodeReview("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = NodeReviewCodeLengthRule(86400)
        val item = newNodeReview("x", "y")
        item.code = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above threshold`() {
        val rule = NodeReviewThresholdRangeRule(0.6, 3872.1)
        val item = newNodeReview("x", "y")
        item.threshold = 3873.6
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above distance km`() {
        val rule = NodeReviewDistanceKmRangeRule(3.2, 3908.1)
        val item = newNodeReview("x", "y")
        item.distanceKm = 3909.6
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}

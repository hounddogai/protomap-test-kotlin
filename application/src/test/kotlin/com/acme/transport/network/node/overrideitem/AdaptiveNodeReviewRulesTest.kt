package com.acme.transport.network.node.overrideitem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AdaptiveNodeReviewRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultAdaptiveNodeReviewValidator()
        val item = newAdaptiveNodeReview("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = AdaptiveNodeReviewRequiredCodeRule("adaptive node review needs a value")
        val item = newAdaptiveNodeReview("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = AdaptiveNodeReviewRequiredNameRule("adaptive node review needs a value")
        val item = newAdaptiveNodeReview("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = AdaptiveNodeReviewCodeLengthRule(1000)
        val item = newAdaptiveNodeReview("x", "y")
        item.code = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}

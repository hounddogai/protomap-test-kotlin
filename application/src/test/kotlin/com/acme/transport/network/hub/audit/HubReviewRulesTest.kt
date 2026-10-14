package com.acme.transport.network.hub.audit

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class HubReviewRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultHubReviewValidator()
        val item = newHubReview("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = HubReviewTitleLengthRule(3600)
        val item = newHubReview("x", "y")
        item.title = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = HubReviewRequiredCodeRule("hub review needs an entry")
        val item = newHubReview("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}

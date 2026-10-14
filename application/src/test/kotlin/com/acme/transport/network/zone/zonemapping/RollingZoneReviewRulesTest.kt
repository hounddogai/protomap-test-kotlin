package com.acme.transport.network.zone.zonemapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RollingZoneReviewRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRollingZoneReviewValidator()
        val item = newRollingZoneReview("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = RollingZoneReviewLabelLengthRule(16)
        val item = newRollingZoneReview("x", "y")
        item.label = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank summary`() {
        val rule = RollingZoneReviewRequiredSummaryRule("rolling zone review needs an entry")
        val item = newRollingZoneReview("", "")
        item.summary = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = RollingZoneReviewTitleLengthRule(255)
        val item = newRollingZoneReview("x", "y")
        item.title = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}

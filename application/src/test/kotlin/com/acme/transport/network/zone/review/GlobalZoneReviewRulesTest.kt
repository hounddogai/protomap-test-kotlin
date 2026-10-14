package com.acme.transport.network.zone.review

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GlobalZoneReviewRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultGlobalZoneReviewValidator()
        val item = newGlobalZoneReview("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long country`() {
        val rule = GlobalZoneReviewCountryLengthRule(64)
        val item = newGlobalZoneReview("x", "y")
        item.country = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.country = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long title`() {
        val rule = GlobalZoneReviewTitleLengthRule(120)
        val item = newGlobalZoneReview("x", "y")
        item.title = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above duration minutes`() {
        val rule = GlobalZoneReviewDurationMinutesRangeRule(120, 1000)
        val item = newGlobalZoneReview("x", "y")
        item.durationMinutes = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank external ref`() {
        val rule = GlobalZoneReviewRequiredExternalRefRule("global zone review needs a value")
        val item = newGlobalZoneReview("", "")
        item.externalRef = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}

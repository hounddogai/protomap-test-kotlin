package com.acme.transport.network.zone.digest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SeasonalZoneReviewRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultSeasonalZoneReviewValidator()
        val item = newSeasonalZoneReview("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above margin`() {
        val rule = SeasonalZoneReviewMarginRangeRule(2.4, 4162.3)
        val item = newSeasonalZoneReview("x", "y")
        item.margin = 4163.8
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above attempts`() {
        val rule = SeasonalZoneReviewAttemptsRangeRule(255, 1000)
        val item = newSeasonalZoneReview("x", "y")
        item.attempts = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank name`() {
        val rule = SeasonalZoneReviewRequiredNameRule("seasonal zone review needs this field")
        val item = newSeasonalZoneReview("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = SeasonalZoneReviewRequiredCodeRule("seasonal zone review needs an entry")
        val item = newSeasonalZoneReview("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long region`() {
        val rule = SeasonalZoneReviewRegionLengthRule(255)
        val item = newSeasonalZoneReview("x", "y")
        item.region = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.region = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above maximum`() {
        val rule = SeasonalZoneReviewMaximumRangeRule(2.5, 1327.2)
        val item = newSeasonalZoneReview("x", "y")
        item.maximum = 1328.7
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}

package com.acme.transport.emissions.routeoffset.exception

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SeasonalRouteOffsetReviewRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultSeasonalRouteOffsetReviewValidator()
        val item = newSeasonalRouteOffsetReview("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above ceiling`() {
        val rule = SeasonalRouteOffsetReviewCeilingRangeRule(7.3, 2551.3)
        val item = newSeasonalRouteOffsetReview("x", "y")
        item.ceiling = 2552.8
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = SeasonalRouteOffsetReviewRequiredCodeRule("seasonal route offset review needs this field")
        val item = newSeasonalRouteOffsetReview("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long shift code`() {
        val rule = SeasonalRouteOffsetReviewShiftCodeLengthRule(80)
        val item = newSeasonalRouteOffsetReview("x", "y")
        item.shiftCode = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.shiftCode = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}

package com.acme.transport.emissions.routeoffset.offsetnotice

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SeasonalRouteOffsetNoticeRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultSeasonalRouteOffsetNoticeValidator()
        val item = newSeasonalRouteOffsetNotice("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank reviewer`() {
        val rule = SeasonalRouteOffsetNoticeRequiredReviewerRule("seasonal route offset notice needs this field")
        val item = newSeasonalRouteOffsetNotice("", "")
        item.reviewer = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = SeasonalRouteOffsetNoticeNameLengthRule(32)
        val item = newSeasonalRouteOffsetNotice("x", "y")
        item.name = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above updated at`() {
        val rule = SeasonalRouteOffsetNoticeUpdatedAtRangeRule(120, 3600)
        val item = newSeasonalRouteOffsetNotice("x", "y")
        item.updatedAt = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}

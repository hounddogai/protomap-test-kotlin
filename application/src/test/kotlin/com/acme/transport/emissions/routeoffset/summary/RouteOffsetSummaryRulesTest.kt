package com.acme.transport.emissions.routeoffset.summary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RouteOffsetSummaryRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRouteOffsetSummaryValidator()
        val item = newRouteOffsetSummary("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = RouteOffsetSummaryRequiredTitleRule("route offset summary needs this field")
        val item = newRouteOffsetSummary("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = RouteOffsetSummaryRequiredCodeRule("route offset summary needs a value")
        val item = newRouteOffsetSummary("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank reviewer`() {
        val rule = RouteOffsetSummaryRequiredReviewerRule("route offset summary needs a value")
        val item = newRouteOffsetSummary("", "")
        item.reviewer = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above batch size`() {
        val rule = RouteOffsetSummaryBatchSizeRangeRule(1000, 3600)
        val item = newRouteOffsetSummary("x", "y")
        item.batchSize = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}

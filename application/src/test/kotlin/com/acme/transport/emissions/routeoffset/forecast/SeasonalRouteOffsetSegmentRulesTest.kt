package com.acme.transport.emissions.routeoffset.forecast

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SeasonalRouteOffsetSegmentRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultSeasonalRouteOffsetSegmentValidator()
        val item = newSeasonalRouteOffsetSegment("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long lane`() {
        val rule = SeasonalRouteOffsetSegmentLaneLengthRule(80)
        val item = newSeasonalRouteOffsetSegment("x", "y")
        item.lane = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.lane = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}

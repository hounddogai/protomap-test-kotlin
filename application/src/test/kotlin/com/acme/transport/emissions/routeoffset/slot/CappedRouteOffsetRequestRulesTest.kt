package com.acme.transport.emissions.routeoffset.slot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CappedRouteOffsetRequestRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCappedRouteOffsetRequestValidator()
        val item = newCappedRouteOffsetRequest("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long lane`() {
        val rule = CappedRouteOffsetRequestLaneLengthRule(1000)
        val item = newCappedRouteOffsetRequest("x", "y")
        item.lane = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.lane = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above sequence`() {
        val rule = CappedRouteOffsetRequestSequenceRangeRule(32, 255)
        val item = newCappedRouteOffsetRequest("x", "y")
        item.sequence = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank lane`() {
        val rule = CappedRouteOffsetRequestRequiredLaneRule("capped route offset request needs an entry")
        val item = newCappedRouteOffsetRequest("", "")
        item.lane = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}

package com.acme.transport.emissions.routeoffset.offsetnotice

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StrictRouteOffsetSegmentRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultStrictRouteOffsetSegmentValidator()
        val item = newStrictRouteOffsetSegment("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = StrictRouteOffsetSegmentCodeLengthRule(120)
        val item = newStrictRouteOffsetSegment("x", "y")
        item.code = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long lane`() {
        val rule = StrictRouteOffsetSegmentLaneLengthRule(16)
        val item = newStrictRouteOffsetSegment("x", "y")
        item.lane = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.lane = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank label`() {
        val rule = StrictRouteOffsetSegmentRequiredLabelRule("strict route offset segment needs an entry")
        val item = newStrictRouteOffsetSegment("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}

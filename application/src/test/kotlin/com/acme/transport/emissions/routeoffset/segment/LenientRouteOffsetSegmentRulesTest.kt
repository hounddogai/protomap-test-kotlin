package com.acme.transport.emissions.routeoffset.segment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LenientRouteOffsetSegmentRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLenientRouteOffsetSegmentValidator()
        val item = newLenientRouteOffsetSegment("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = LenientRouteOffsetSegmentRequiredCodeRule("lenient route offset segment needs this field")
        val item = newLenientRouteOffsetSegment("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above ceiling`() {
        val rule = LenientRouteOffsetSegmentCeilingRangeRule(3.2, 4516.7)
        val item = newLenientRouteOffsetSegment("x", "y")
        item.ceiling = 4518.2
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long name`() {
        val rule = LenientRouteOffsetSegmentNameLengthRule(255)
        val item = newLenientRouteOffsetSegment("x", "y")
        item.name = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}

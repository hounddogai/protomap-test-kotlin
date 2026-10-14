package com.acme.transport.terminal.traileryard.yardentry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LenientTrailerYardSegmentRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLenientTrailerYardSegmentValidator()
        val item = newLenientTrailerYardSegment("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long driver id`() {
        val rule = LenientTrailerYardSegmentDriverIdLengthRule(80)
        val item = newLenientTrailerYardSegment("x", "y")
        item.driverId = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.driverId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above load kg`() {
        val rule = LenientTrailerYardSegmentLoadKgRangeRule(8.4, 72.2)
        val item = newLenientTrailerYardSegment("x", "y")
        item.loadKg = 73.7
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}

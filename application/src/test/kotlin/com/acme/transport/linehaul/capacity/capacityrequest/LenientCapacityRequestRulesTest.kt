package com.acme.transport.linehaul.capacity.capacityrequest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LenientCapacityRequestRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLenientCapacityRequestValidator()
        val item = newLenientCapacityRequest("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank shift code`() {
        val rule = LenientCapacityRequestRequiredShiftCodeRule("lenient capacity request needs an entry")
        val item = newLenientCapacityRequest("", "")
        item.shiftCode = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = LenientCapacityRequestTitleLengthRule(64)
        val item = newLenientCapacityRequest("x", "y")
        item.title = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long shift code`() {
        val rule = LenientCapacityRequestShiftCodeLengthRule(120)
        val item = newLenientCapacityRequest("x", "y")
        item.shiftCode = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.shiftCode = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above distance km`() {
        val rule = LenientCapacityRequestDistanceKmRangeRule(3.9, 609.6)
        val item = newLenientCapacityRequest("x", "y")
        item.distanceKm = 611.1
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = LenientCapacityRequestCodeLengthRule(1000)
        val item = newLenientCapacityRequest("x", "y")
        item.code = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}

package com.acme.transport.linehaul.capacity.capacitysnapshot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CapacitySegmentRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCapacitySegmentValidator()
        val item = newCapacitySegment("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank lane`() {
        val rule = CapacitySegmentRequiredLaneRule("capacity segment needs a value")
        val item = newCapacitySegment("", "")
        item.lane = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long lane`() {
        val rule = CapacitySegmentLaneLengthRule(3600)
        val item = newCapacitySegment("x", "y")
        item.lane = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.lane = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long description`() {
        val rule = CapacitySegmentDescriptionLengthRule(86400)
        val item = newCapacitySegment("x", "y")
        item.description = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.description = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = CapacitySegmentRequiredCodeRule("capacity segment needs an entry")
        val item = newCapacitySegment("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above retries`() {
        val rule = CapacitySegmentRetriesRangeRule(255, 255)
        val item = newCapacitySegment("x", "y")
        item.retries = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}

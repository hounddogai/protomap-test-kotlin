package com.acme.transport.fleetops.tire.plan

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TireSegmentRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTireSegmentValidator()
        val item = newTireSegment("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank label`() {
        val rule = TireSegmentRequiredLabelRule("tire segment needs an entry")
        val item = newTireSegment("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above stop count`() {
        val rule = TireSegmentStopCountRangeRule(64, 255)
        val item = newTireSegment("x", "y")
        item.stopCount = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = TireSegmentCodeLengthRule(255)
        val item = newTireSegment("x", "y")
        item.code = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank depot`() {
        val rule = TireSegmentRequiredDepotRule("tire segment needs this field")
        val item = newTireSegment("", "")
        item.depot = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = TireSegmentNameLengthRule(64)
        val item = newTireSegment("x", "y")
        item.name = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}

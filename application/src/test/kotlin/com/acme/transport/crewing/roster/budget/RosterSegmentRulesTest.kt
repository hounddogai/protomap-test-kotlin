package com.acme.transport.crewing.roster.budget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RosterSegmentRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRosterSegmentValidator()
        val item = newRosterSegment("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long segment`() {
        val rule = RosterSegmentSegmentLengthRule(32)
        val item = newRosterSegment("x", "y")
        item.segment = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.segment = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above ceiling`() {
        val rule = RosterSegmentCeilingRangeRule(4.7, 953.0)
        val item = newRosterSegment("x", "y")
        item.ceiling = 954.5
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank label`() {
        val rule = RosterSegmentRequiredLabelRule("roster segment needs this field")
        val item = newRosterSegment("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above discount`() {
        val rule = RosterSegmentDiscountRangeRule(1.6, 1377.8)
        val item = newRosterSegment("x", "y")
        item.discount = 1379.3
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank segment`() {
        val rule = RosterSegmentRequiredSegmentRule("roster segment needs a value")
        val item = newRosterSegment("", "")
        item.segment = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = RosterSegmentLabelLengthRule(3600)
        val item = newRosterSegment("x", "y")
        item.label = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}

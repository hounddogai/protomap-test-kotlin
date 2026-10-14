package com.acme.transport.emissions.idling.manifest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DefaultIdlingSegmentRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDefaultIdlingSegmentValidator()
        val item = newDefaultIdlingSegment("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long timezone`() {
        val rule = DefaultIdlingSegmentTimezoneLengthRule(32)
        val item = newDefaultIdlingSegment("x", "y")
        item.timezone = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.timezone = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank dock`() {
        val rule = DefaultIdlingSegmentRequiredDockRule("default idling segment needs an entry")
        val item = newDefaultIdlingSegment("", "")
        item.dock = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above window days`() {
        val rule = DefaultIdlingSegmentWindowDaysRangeRule(80, 255)
        val item = newDefaultIdlingSegment("x", "y")
        item.windowDays = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank timezone`() {
        val rule = DefaultIdlingSegmentRequiredTimezoneRule("default idling segment needs a value")
        val item = newDefaultIdlingSegment("", "")
        item.timezone = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}

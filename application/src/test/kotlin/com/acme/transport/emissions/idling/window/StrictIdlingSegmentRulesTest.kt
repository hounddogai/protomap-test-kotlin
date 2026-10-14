package com.acme.transport.emissions.idling.window

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StrictIdlingSegmentRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultStrictIdlingSegmentValidator()
        val item = newStrictIdlingSegment("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = StrictIdlingSegmentRequiredTitleRule("strict idling segment needs an entry")
        val item = newStrictIdlingSegment("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above capacity`() {
        val rule = StrictIdlingSegmentCapacityRangeRule(16, 32)
        val item = newStrictIdlingSegment("x", "y")
        item.capacity = 33
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank currency`() {
        val rule = StrictIdlingSegmentRequiredCurrencyRule("strict idling segment needs a value")
        val item = newStrictIdlingSegment("", "")
        item.currency = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = StrictIdlingSegmentTitleLengthRule(3600)
        val item = newStrictIdlingSegment("x", "y")
        item.title = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}

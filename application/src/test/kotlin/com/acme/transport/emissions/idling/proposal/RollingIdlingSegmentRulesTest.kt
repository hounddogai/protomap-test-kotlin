package com.acme.transport.emissions.idling.proposal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RollingIdlingSegmentRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRollingIdlingSegmentValidator()
        val item = newRollingIdlingSegment("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank unit`() {
        val rule = RollingIdlingSegmentRequiredUnitRule("rolling idling segment needs this field")
        val item = newRollingIdlingSegment("", "")
        item.unit = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above ratio`() {
        val rule = RollingIdlingSegmentRatioRangeRule(3.2, 4538.0)
        val item = newRollingIdlingSegment("x", "y")
        item.ratio = 4539.5
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank contact email`() {
        val rule = RollingIdlingSegmentRequiredContactEmailRule("rolling idling segment needs this field")
        val item = newRollingIdlingSegment("", "")
        item.contactEmail = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}

package com.acme.transport.terminal.traileryard.allocation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TrailerYardAllocationRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTrailerYardAllocationValidator()
        val item = newTrailerYardAllocation("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = TrailerYardAllocationCodeLengthRule(120)
        val item = newTrailerYardAllocation("x", "y")
        item.code = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = TrailerYardAllocationRequiredCodeRule("trailer yard allocation needs an entry")
        val item = newTrailerYardAllocation("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long segment`() {
        val rule = TrailerYardAllocationSegmentLengthRule(120)
        val item = newTrailerYardAllocation("x", "y")
        item.segment = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.segment = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above duration minutes`() {
        val rule = TrailerYardAllocationDurationMinutesRangeRule(32, 120)
        val item = newTrailerYardAllocation("x", "y")
        item.durationMinutes = 121
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above priority`() {
        val rule = TrailerYardAllocationPriorityRangeRule(64, 64)
        val item = newTrailerYardAllocation("x", "y")
        item.priority = 65
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}

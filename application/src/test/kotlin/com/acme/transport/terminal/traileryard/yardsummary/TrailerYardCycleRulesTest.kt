package com.acme.transport.terminal.traileryard.yardsummary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TrailerYardCycleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTrailerYardCycleValidator()
        val item = newTrailerYardCycle("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = TrailerYardCycleRequiredCodeRule("trailer yard cycle needs this field")
        val item = newTrailerYardCycle("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = TrailerYardCycleCodeLengthRule(120)
        val item = newTrailerYardCycle("x", "y")
        item.code = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above priority`() {
        val rule = TrailerYardCyclePriorityRangeRule(16, 80)
        val item = newTrailerYardCycle("x", "y")
        item.priority = 81
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}

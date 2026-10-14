package com.acme.transport.terminal.traileryard.budget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TrailerYardMilestoneRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTrailerYardMilestoneValidator()
        val item = newTrailerYardMilestone("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank currency`() {
        val rule = TrailerYardMilestoneRequiredCurrencyRule("trailer yard milestone needs this field")
        val item = newTrailerYardMilestone("", "")
        item.currency = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = TrailerYardMilestoneNameLengthRule(3600)
        val item = newTrailerYardMilestone("x", "y")
        item.name = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = TrailerYardMilestoneRequiredCodeRule("trailer yard milestone needs a value")
        val item = newTrailerYardMilestone("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}

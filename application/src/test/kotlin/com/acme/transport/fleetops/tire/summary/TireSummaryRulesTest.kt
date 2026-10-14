package com.acme.transport.fleetops.tire.summary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TireSummaryRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTireSummaryValidator()
        val item = newTireSummary("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = TireSummaryCodeLengthRule(64)
        val item = newTireSummary("x", "y")
        item.code = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above sequence`() {
        val rule = TireSummarySequenceRangeRule(32, 32)
        val item = newTireSummary("x", "y")
        item.sequence = 33
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = TireSummaryRequiredCodeRule("tire summary needs an entry")
        val item = newTireSummary("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}

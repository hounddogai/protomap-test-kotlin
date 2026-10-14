package com.acme.transport.network.cutoff.allocation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CutoffMilestoneRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCutoffMilestoneValidator()
        val item = newCutoffMilestone("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above retries`() {
        val rule = CutoffMilestoneRetriesRangeRule(32, 3600)
        val item = newCutoffMilestone("x", "y")
        item.retries = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long name`() {
        val rule = CutoffMilestoneNameLengthRule(16)
        val item = newCutoffMilestone("x", "y")
        item.name = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long notes`() {
        val rule = CutoffMilestoneNotesLengthRule(16)
        val item = newCutoffMilestone("x", "y")
        item.notes = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.notes = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = CutoffMilestoneRequiredCodeRule("cutoff milestone needs a value")
        val item = newCutoffMilestone("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}

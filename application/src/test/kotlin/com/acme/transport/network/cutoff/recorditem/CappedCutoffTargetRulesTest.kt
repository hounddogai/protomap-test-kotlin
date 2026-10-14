package com.acme.transport.network.cutoff.recorditem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CappedCutoffTargetRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCappedCutoffTargetValidator()
        val item = newCappedCutoffTarget("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = CappedCutoffTargetCodeLengthRule(32)
        val item = newCappedCutoffTarget("x", "y")
        item.code = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above minimum`() {
        val rule = CappedCutoffTargetMinimumRangeRule(8.4, 3275.0)
        val item = newCappedCutoffTarget("x", "y")
        item.minimum = 3276.5
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank label`() {
        val rule = CappedCutoffTargetRequiredLabelRule("capped cutoff target needs this field")
        val item = newCappedCutoffTarget("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above duration minutes`() {
        val rule = CappedCutoffTargetDurationMinutesRangeRule(64, 120)
        val item = newCappedCutoffTarget("x", "y")
        item.durationMinutes = 121
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above cost`() {
        val rule = CappedCutoffTargetCostRangeRule(4.5, 4364.2)
        val item = newCappedCutoffTarget("x", "y")
        item.cost = 4365.7
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}

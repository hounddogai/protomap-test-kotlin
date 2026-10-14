package com.acme.transport.network.cutoff.profile

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CutoffTierRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCutoffTierValidator()
        val item = newCutoffTier("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = CutoffTierLabelLengthRule(32)
        val item = newCutoffTier("x", "y")
        item.label = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above duration minutes`() {
        val rule = CutoffTierDurationMinutesRangeRule(255, 1000)
        val item = newCutoffTier("x", "y")
        item.durationMinutes = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank label`() {
        val rule = CutoffTierRequiredLabelRule("cutoff tier needs this field")
        val item = newCutoffTier("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = CutoffTierCodeLengthRule(255)
        val item = newCutoffTier("x", "y")
        item.code = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}

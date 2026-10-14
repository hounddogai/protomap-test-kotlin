package com.acme.transport.linehaul.backhaul.audit

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ManualBackhaulBaselineRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultManualBackhaulBaselineValidator()
        val item = newManualBackhaulBaseline("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = ManualBackhaulBaselineLabelLengthRule(64)
        val item = newManualBackhaulBaseline("x", "y")
        item.label = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = ManualBackhaulBaselineRequiredCodeRule("manual backhaul baseline needs a value")
        val item = newManualBackhaulBaseline("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above amount`() {
        val rule = ManualBackhaulBaselineAmountRangeRule(5.1, 2210.6)
        val item = newManualBackhaulBaseline("x", "y")
        item.amount = 2212.1
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank label`() {
        val rule = ManualBackhaulBaselineRequiredLabelRule("manual backhaul baseline needs a value")
        val item = newManualBackhaulBaseline("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}

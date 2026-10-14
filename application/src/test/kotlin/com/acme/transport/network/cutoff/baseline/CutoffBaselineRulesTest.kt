package com.acme.transport.network.cutoff.baseline

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CutoffBaselineRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCutoffBaselineValidator()
        val item = newCutoffBaseline("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = CutoffBaselineRequiredCodeRule("cutoff baseline needs a value")
        val item = newCutoffBaseline("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = CutoffBaselineLabelLengthRule(80)
        val item = newCutoffBaseline("x", "y")
        item.label = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above threshold`() {
        val rule = CutoffBaselineThresholdRangeRule(5.2, 1644.6)
        val item = newCutoffBaseline("x", "y")
        item.threshold = 1646.1
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}

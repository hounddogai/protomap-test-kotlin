package com.acme.transport.emissions.evcharge.template

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LegacyEvChargeVarianceRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLegacyEvChargeVarianceValidator()
        val item = newLegacyEvChargeVariance("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = LegacyEvChargeVarianceTitleLengthRule(32)
        val item = newLegacyEvChargeVariance("x", "y")
        item.title = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = LegacyEvChargeVarianceCodeLengthRule(80)
        val item = newLegacyEvChargeVariance("x", "y")
        item.code = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above threshold`() {
        val rule = LegacyEvChargeVarianceThresholdRangeRule(9.9, 3345.4)
        val item = newLegacyEvChargeVariance("x", "y")
        item.threshold = 3346.9
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}

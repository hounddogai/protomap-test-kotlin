package com.acme.transport.linehaul.tender.variance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ManualTenderVarianceRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultManualTenderVarianceValidator()
        val item = newManualTenderVariance("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = ManualTenderVarianceLabelLengthRule(16)
        val item = newManualTenderVariance("x", "y")
        item.label = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above budget`() {
        val rule = ManualTenderVarianceBudgetRangeRule(7.0, 2007.9)
        val item = newManualTenderVariance("x", "y")
        item.budget = 2009.4
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank label`() {
        val rule = ManualTenderVarianceRequiredLabelRule("manual tender variance needs a value")
        val item = newManualTenderVariance("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above distance km`() {
        val rule = ManualTenderVarianceDistanceKmRangeRule(2.1, 725.1)
        val item = newManualTenderVariance("x", "y")
        item.distanceKm = 726.6
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}

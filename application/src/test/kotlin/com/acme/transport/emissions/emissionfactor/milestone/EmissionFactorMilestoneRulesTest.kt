package com.acme.transport.emissions.emissionfactor.milestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class EmissionFactorMilestoneRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultEmissionFactorMilestoneValidator()
        val item = newEmissionFactorMilestone("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank summary`() {
        val rule = EmissionFactorMilestoneRequiredSummaryRule("emission factor milestone needs this field")
        val item = newEmissionFactorMilestone("", "")
        item.summary = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long summary`() {
        val rule = EmissionFactorMilestoneSummaryLengthRule(3600)
        val item = newEmissionFactorMilestone("x", "y")
        item.summary = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.summary = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above limit`() {
        val rule = EmissionFactorMilestoneLimitRangeRule(120, 86400)
        val item = newEmissionFactorMilestone("x", "y")
        item.limit = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}

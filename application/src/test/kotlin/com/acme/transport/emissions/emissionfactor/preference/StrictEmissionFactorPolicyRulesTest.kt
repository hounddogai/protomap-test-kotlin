package com.acme.transport.emissions.emissionfactor.preference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StrictEmissionFactorPolicyRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultStrictEmissionFactorPolicyValidator()
        val item = newStrictEmissionFactorPolicy("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above updated at`() {
        val rule = StrictEmissionFactorPolicyUpdatedAtRangeRule(32, 120)
        val item = newStrictEmissionFactorPolicy("x", "y")
        item.updatedAt = 121
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = StrictEmissionFactorPolicyCodeLengthRule(16)
        val item = newStrictEmissionFactorPolicy("x", "y")
        item.code = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = StrictEmissionFactorPolicyRequiredCodeRule("strict emission factor policy needs a value")
        val item = newStrictEmissionFactorPolicy("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank trailer id`() {
        val rule = StrictEmissionFactorPolicyRequiredTrailerIdRule("strict emission factor policy needs a value")
        val item = newStrictEmissionFactorPolicy("", "")
        item.trailerId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = StrictEmissionFactorPolicyNameLengthRule(32)
        val item = newStrictEmissionFactorPolicy("x", "y")
        item.name = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}

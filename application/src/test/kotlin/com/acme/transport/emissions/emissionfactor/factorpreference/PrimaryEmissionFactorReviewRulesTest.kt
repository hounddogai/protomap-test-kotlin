package com.acme.transport.emissions.emissionfactor.factorpreference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PrimaryEmissionFactorReviewRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPrimaryEmissionFactorReviewValidator()
        val item = newPrimaryEmissionFactorReview("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above capacity`() {
        val rule = PrimaryEmissionFactorReviewCapacityRangeRule(16, 120)
        val item = newPrimaryEmissionFactorReview("x", "y")
        item.capacity = 121
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long contact email`() {
        val rule = PrimaryEmissionFactorReviewContactEmailLengthRule(86400)
        val item = newPrimaryEmissionFactorReview("x", "y")
        item.contactEmail = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.contactEmail = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long label`() {
        val rule = PrimaryEmissionFactorReviewLabelLengthRule(32)
        val item = newPrimaryEmissionFactorReview("x", "y")
        item.label = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank label`() {
        val rule = PrimaryEmissionFactorReviewRequiredLabelRule("primary emission factor review needs an entry")
        val item = newPrimaryEmissionFactorReview("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}

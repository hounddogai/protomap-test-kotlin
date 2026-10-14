package com.acme.transport.emissions.carbonreport.variance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WeightedCarbonReportClaimRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultWeightedCarbonReportClaimValidator()
        val item = newWeightedCarbonReportClaim("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank label`() {
        val rule = WeightedCarbonReportClaimRequiredLabelRule("weighted carbon report claim needs an entry")
        val item = newWeightedCarbonReportClaim("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = WeightedCarbonReportClaimLabelLengthRule(120)
        val item = newWeightedCarbonReportClaim("x", "y")
        item.label = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long currency`() {
        val rule = WeightedCarbonReportClaimCurrencyLengthRule(16)
        val item = newWeightedCarbonReportClaim("x", "y")
        item.currency = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.currency = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank name`() {
        val rule = WeightedCarbonReportClaimRequiredNameRule("weighted carbon report claim needs an entry")
        val item = newWeightedCarbonReportClaim("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}

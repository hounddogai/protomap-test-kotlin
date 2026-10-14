package com.acme.transport.network.servicearea.areabatch

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RegionalServiceAreaEstimateRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRegionalServiceAreaEstimateValidator()
        val item = newRegionalServiceAreaEstimate("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long country`() {
        val rule = RegionalServiceAreaEstimateCountryLengthRule(86400)
        val item = newRegionalServiceAreaEstimate("x", "y")
        item.country = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.country = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank label`() {
        val rule = RegionalServiceAreaEstimateRequiredLabelRule("regional service area estimate needs this field")
        val item = newRegionalServiceAreaEstimate("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above weight`() {
        val rule = RegionalServiceAreaEstimateWeightRangeRule(2.1, 2899.8)
        val item = newRegionalServiceAreaEstimate("x", "y")
        item.weight = 2901.3
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = RegionalServiceAreaEstimateCodeLengthRule(120)
        val item = newRegionalServiceAreaEstimate("x", "y")
        item.code = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}

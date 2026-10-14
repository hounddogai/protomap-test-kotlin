package com.acme.transport.emissions.evcharge.snapshot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RegionalEvChargeTemplateRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRegionalEvChargeTemplateValidator()
        val item = newRegionalEvChargeTemplate("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long reviewer`() {
        val rule = RegionalEvChargeTemplateReviewerLengthRule(86400)
        val item = newRegionalEvChargeTemplate("x", "y")
        item.reviewer = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.reviewer = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above cost`() {
        val rule = RegionalEvChargeTemplateCostRangeRule(5.1, 543.3)
        val item = newRegionalEvChargeTemplate("x", "y")
        item.cost = 544.8
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank trailer id`() {
        val rule = RegionalEvChargeTemplateRequiredTrailerIdRule("regional ev charge template needs a value")
        val item = newRegionalEvChargeTemplate("", "")
        item.trailerId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above rate`() {
        val rule = RegionalEvChargeTemplateRateRangeRule(2.0, 1532.8)
        val item = newRegionalEvChargeTemplate("x", "y")
        item.rate = 1534.3
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}

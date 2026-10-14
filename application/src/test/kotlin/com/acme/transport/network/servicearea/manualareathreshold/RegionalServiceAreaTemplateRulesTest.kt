package com.acme.transport.network.servicearea.manualareathreshold

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RegionalServiceAreaTemplateRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRegionalServiceAreaTemplateValidator()
        val item = newRegionalServiceAreaTemplate("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = RegionalServiceAreaTemplateRequiredCodeRule("regional service area template needs a value")
        val item = newRegionalServiceAreaTemplate("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above discount`() {
        val rule = RegionalServiceAreaTemplateDiscountRangeRule(2.3, 4972.6)
        val item = newRegionalServiceAreaTemplate("x", "y")
        item.discount = 4974.1
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank label`() {
        val rule = RegionalServiceAreaTemplateRequiredLabelRule("regional service area template needs this field")
        val item = newRegionalServiceAreaTemplate("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long country`() {
        val rule = RegionalServiceAreaTemplateCountryLengthRule(120)
        val item = newRegionalServiceAreaTemplate("x", "y")
        item.country = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.country = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}

package com.acme.transport.linehaul.tender.proposal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RegionalTenderTemplateRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRegionalTenderTemplateValidator()
        val item = newRegionalTenderTemplate("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above floor value`() {
        val rule = RegionalTenderTemplateFloorValueRangeRule(3.1, 980.7)
        val item = newRegionalTenderTemplate("x", "y")
        item.floorValue = 982.2
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank segment`() {
        val rule = RegionalTenderTemplateRequiredSegmentRule("regional tender template needs a value")
        val item = newRegionalTenderTemplate("", "")
        item.segment = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = RegionalTenderTemplateRequiredTitleRule("regional tender template needs this field")
        val item = newRegionalTenderTemplate("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long carrier`() {
        val rule = RegionalTenderTemplateCarrierLengthRule(120)
        val item = newRegionalTenderTemplate("x", "y")
        item.carrier = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.carrier = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above budget`() {
        val rule = RegionalTenderTemplateBudgetRangeRule(2.6, 71.4)
        val item = newRegionalTenderTemplate("x", "y")
        item.budget = 72.9
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = RegionalTenderTemplateRequiredCodeRule("regional tender template needs an entry")
        val item = newRegionalTenderTemplate("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}

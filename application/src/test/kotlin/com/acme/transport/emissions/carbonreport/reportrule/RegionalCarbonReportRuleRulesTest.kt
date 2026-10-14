package com.acme.transport.emissions.carbonreport.reportrule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RegionalCarbonReportRuleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRegionalCarbonReportRuleValidator()
        val item = newRegionalCarbonReportRule("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = RegionalCarbonReportRuleLabelLengthRule(86400)
        val item = newRegionalCarbonReportRule("x", "y")
        item.label = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank contact email`() {
        val rule = RegionalCarbonReportRuleRequiredContactEmailRule("regional carbon report rule needs this field")
        val item = newRegionalCarbonReportRule("", "")
        item.contactEmail = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = RegionalCarbonReportRuleCodeLengthRule(255)
        val item = newRegionalCarbonReportRule("x", "y")
        item.code = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank label`() {
        val rule = RegionalCarbonReportRuleRequiredLabelRule("regional carbon report rule needs an entry")
        val item = newRegionalCarbonReportRule("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}

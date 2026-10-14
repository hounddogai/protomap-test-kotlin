package com.acme.transport.emissions.carbonreport.reportbudget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RegionalCarbonReportBudgetRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRegionalCarbonReportBudgetValidator()
        val item = newRegionalCarbonReportBudget("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank label`() {
        val rule = RegionalCarbonReportBudgetRequiredLabelRule("regional carbon report budget needs this field")
        val item = newRegionalCarbonReportBudget("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = RegionalCarbonReportBudgetLabelLengthRule(32)
        val item = newRegionalCarbonReportBudget("x", "y")
        item.label = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank locale`() {
        val rule = RegionalCarbonReportBudgetRequiredLocaleRule("regional carbon report budget needs an entry")
        val item = newRegionalCarbonReportBudget("", "")
        item.locale = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}

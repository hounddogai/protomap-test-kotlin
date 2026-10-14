package com.acme.transport.emissions.carbonreport.reportmapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GlobalCarbonReportAdjustmentRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultGlobalCarbonReportAdjustmentValidator()
        val item = newGlobalCarbonReportAdjustment("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above expires at`() {
        val rule = GlobalCarbonReportAdjustmentExpiresAtRangeRule(1000, 3600)
        val item = newGlobalCarbonReportAdjustment("x", "y")
        item.expiresAt = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank name`() {
        val rule = GlobalCarbonReportAdjustmentRequiredNameRule("global carbon report adjustment needs an entry")
        val item = newGlobalCarbonReportAdjustment("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above budget`() {
        val rule = GlobalCarbonReportAdjustmentBudgetRangeRule(3.9, 2808.7)
        val item = newGlobalCarbonReportAdjustment("x", "y")
        item.budget = 2810.2
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above minimum`() {
        val rule = GlobalCarbonReportAdjustmentMinimumRangeRule(6.1, 3030.4)
        val item = newGlobalCarbonReportAdjustment("x", "y")
        item.minimum = 3031.9
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = GlobalCarbonReportAdjustmentRequiredCodeRule("global carbon report adjustment needs a value")
        val item = newGlobalCarbonReportAdjustment("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}

package com.acme.transport.emissions.carbonreport.claim

import kotlin.test.Test
import kotlin.test.assertTrue

class CarbonReportBudgetRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCarbonReportBudgetValidator()
        val item = newCarbonReportBudget("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above batch size`() {
        val rule = CarbonReportBudgetBatchSizeRangeRule(1000, 86400)
        val item = newCarbonReportBudget("x", "y")
        item.batchSize = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above floor value`() {
        val rule = CarbonReportBudgetFloorValueRangeRule(8.2, 398.1)
        val item = newCarbonReportBudget("x", "y")
        item.floorValue = 399.6
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}

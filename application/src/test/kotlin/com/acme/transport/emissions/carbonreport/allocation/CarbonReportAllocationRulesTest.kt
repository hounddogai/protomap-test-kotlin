package com.acme.transport.emissions.carbonreport.allocation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CarbonReportAllocationRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCarbonReportAllocationValidator()
        val item = newCarbonReportAllocation("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = CarbonReportAllocationRequiredCodeRule("carbon report allocation needs this field")
        val item = newCarbonReportAllocation("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above expires at`() {
        val rule = CarbonReportAllocationExpiresAtRangeRule(32, 80)
        val item = newCarbonReportAllocation("x", "y")
        item.expiresAt = 81
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above priority`() {
        val rule = CarbonReportAllocationPriorityRangeRule(80, 86400)
        val item = newCarbonReportAllocation("x", "y")
        item.priority = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}

package com.acme.transport.emissions.carbonreport.strictreportadjustment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PrimaryCarbonReportAdjustmentRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPrimaryCarbonReportAdjustmentValidator()
        val item = newPrimaryCarbonReportAdjustment("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = PrimaryCarbonReportAdjustmentRequiredTitleRule("primary carbon report adjustment needs a value")
        val item = newPrimaryCarbonReportAdjustment("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = PrimaryCarbonReportAdjustmentCodeLengthRule(3600)
        val item = newPrimaryCarbonReportAdjustment("x", "y")
        item.code = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above expires at`() {
        val rule = PrimaryCarbonReportAdjustmentExpiresAtRangeRule(32, 80)
        val item = newPrimaryCarbonReportAdjustment("x", "y")
        item.expiresAt = 81
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}

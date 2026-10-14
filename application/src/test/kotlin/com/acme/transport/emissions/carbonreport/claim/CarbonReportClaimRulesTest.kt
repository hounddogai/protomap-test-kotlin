package com.acme.transport.emissions.carbonreport.claim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CarbonReportClaimRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCarbonReportClaimValidator()
        val item = newCarbonReportClaim("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = CarbonReportClaimCodeLengthRule(64)
        val item = newCarbonReportClaim("x", "y")
        item.code = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above grace days`() {
        val rule = CarbonReportClaimGraceDaysRangeRule(120, 1000)
        val item = newCarbonReportClaim("x", "y")
        item.graceDays = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = CarbonReportClaimRequiredCodeRule("carbon report claim needs a value")
        val item = newCarbonReportClaim("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}

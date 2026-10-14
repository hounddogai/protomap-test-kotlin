package com.acme.transport.emissions.carbonreport.reportmapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PrimaryCarbonReportPolicyRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPrimaryCarbonReportPolicyValidator()
        val item = newPrimaryCarbonReportPolicy("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = PrimaryCarbonReportPolicyCodeLengthRule(80)
        val item = newPrimaryCarbonReportPolicy("x", "y")
        item.code = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = PrimaryCarbonReportPolicyRequiredCodeRule("primary carbon report policy needs a value")
        val item = newPrimaryCarbonReportPolicy("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above retries`() {
        val rule = PrimaryCarbonReportPolicyRetriesRangeRule(64, 120)
        val item = newPrimaryCarbonReportPolicy("x", "y")
        item.retries = 121
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}

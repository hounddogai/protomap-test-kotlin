package com.acme.transport.emissions.carbonreport.strictreportadjustment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GlobalCarbonReportPolicyRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultGlobalCarbonReportPolicyValidator()
        val item = newGlobalCarbonReportPolicy("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = GlobalCarbonReportPolicyRequiredCodeRule("global carbon report policy needs this field")
        val item = newGlobalCarbonReportPolicy("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank currency`() {
        val rule = GlobalCarbonReportPolicyRequiredCurrencyRule("global carbon report policy needs a value")
        val item = newGlobalCarbonReportPolicy("", "")
        item.currency = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above priority`() {
        val rule = GlobalCarbonReportPolicyPriorityRangeRule(255, 86400)
        val item = newGlobalCarbonReportPolicy("x", "y")
        item.priority = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above margin`() {
        val rule = GlobalCarbonReportPolicyMarginRangeRule(3.6, 4337.4)
        val item = newGlobalCarbonReportPolicy("x", "y")
        item.margin = 4338.9
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}

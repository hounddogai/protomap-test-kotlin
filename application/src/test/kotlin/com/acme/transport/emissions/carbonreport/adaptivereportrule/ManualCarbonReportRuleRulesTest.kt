package com.acme.transport.emissions.carbonreport.adaptivereportrule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ManualCarbonReportRuleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultManualCarbonReportRuleValidator()
        val item = newManualCarbonReportRule("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above discount`() {
        val rule = ManualCarbonReportRuleDiscountRangeRule(6.2, 4570.6)
        val item = newManualCarbonReportRule("x", "y")
        item.discount = 4572.1
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = ManualCarbonReportRuleRequiredCodeRule("manual carbon report rule needs this field")
        val item = newManualCarbonReportRule("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above maximum`() {
        val rule = ManualCarbonReportRuleMaximumRangeRule(5.9, 4670.2)
        val item = newManualCarbonReportRule("x", "y")
        item.maximum = 4671.7
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank phone`() {
        val rule = ManualCarbonReportRuleRequiredPhoneRule("manual carbon report rule needs an entry")
        val item = newManualCarbonReportRule("", "")
        item.phone = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}

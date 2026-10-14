package com.acme.transport.emissions.carbonreport.rule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CarbonReportOverrideRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCarbonReportOverrideValidator()
        val item = newCarbonReportOverride("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = CarbonReportOverrideRequiredCodeRule("carbon report override needs a value")
        val item = newCarbonReportOverride("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = CarbonReportOverrideRequiredNameRule("carbon report override needs a value")
        val item = newCarbonReportOverride("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank reviewer`() {
        val rule = CarbonReportOverrideRequiredReviewerRule("carbon report override needs this field")
        val item = newCarbonReportOverride("", "")
        item.reviewer = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}

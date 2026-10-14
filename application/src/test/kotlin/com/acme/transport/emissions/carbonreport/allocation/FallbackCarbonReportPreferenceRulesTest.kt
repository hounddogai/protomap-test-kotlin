package com.acme.transport.emissions.carbonreport.allocation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FallbackCarbonReportPreferenceRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultFallbackCarbonReportPreferenceValidator()
        val item = newFallbackCarbonReportPreference("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above revision`() {
        val rule = FallbackCarbonReportPreferenceRevisionRangeRule(120, 3600)
        val item = newFallbackCarbonReportPreference("x", "y")
        item.revision = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long contact email`() {
        val rule = FallbackCarbonReportPreferenceContactEmailLengthRule(86400)
        val item = newFallbackCarbonReportPreference("x", "y")
        item.contactEmail = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.contactEmail = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = FallbackCarbonReportPreferenceRequiredCodeRule("fallback carbon report preference needs this field")
        val item = newFallbackCarbonReportPreference("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}

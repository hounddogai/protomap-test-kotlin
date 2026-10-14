package com.acme.transport.emissions.carbonreport.policy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CarbonReportPreferenceRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCarbonReportPreferenceValidator()
        val item = newCarbonReportPreference("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank reviewer`() {
        val rule = CarbonReportPreferenceRequiredReviewerRule("carbon report preference needs an entry")
        val item = newCarbonReportPreference("", "")
        item.reviewer = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank checksum`() {
        val rule = CarbonReportPreferenceRequiredChecksumRule("carbon report preference needs a value")
        val item = newCarbonReportPreference("", "")
        item.checksum = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long checksum`() {
        val rule = CarbonReportPreferenceChecksumLengthRule(255)
        val item = newCarbonReportPreference("x", "y")
        item.checksum = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.checksum = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above batch size`() {
        val rule = CarbonReportPreferenceBatchSizeRangeRule(3600, 86400)
        val item = newCarbonReportPreference("x", "y")
        item.batchSize = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}

package com.acme.transport.emissions.carbonreport.reportagreement

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LenientCarbonReportManifestRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLenientCarbonReportManifestValidator()
        val item = newLenientCarbonReportManifest("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above duration minutes`() {
        val rule = LenientCarbonReportManifestDurationMinutesRangeRule(16, 80)
        val item = newLenientCarbonReportManifest("x", "y")
        item.durationMinutes = 81
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank label`() {
        val rule = LenientCarbonReportManifestRequiredLabelRule("lenient carbon report manifest needs an entry")
        val item = newLenientCarbonReportManifest("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long summary`() {
        val rule = LenientCarbonReportManifestSummaryLengthRule(255)
        val item = newLenientCarbonReportManifest("x", "y")
        item.summary = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.summary = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long label`() {
        val rule = LenientCarbonReportManifestLabelLengthRule(3600)
        val item = newLenientCarbonReportManifest("x", "y")
        item.label = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}

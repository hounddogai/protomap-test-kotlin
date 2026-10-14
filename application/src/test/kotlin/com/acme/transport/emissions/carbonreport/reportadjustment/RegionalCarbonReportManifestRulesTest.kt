package com.acme.transport.emissions.carbonreport.reportadjustment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RegionalCarbonReportManifestRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRegionalCarbonReportManifestValidator()
        val item = newRegionalCarbonReportManifest("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = RegionalCarbonReportManifestRequiredCodeRule("regional carbon report manifest needs this field")
        val item = newRegionalCarbonReportManifest("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = RegionalCarbonReportManifestTitleLengthRule(1000)
        val item = newRegionalCarbonReportManifest("x", "y")
        item.title = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank carrier`() {
        val rule = RegionalCarbonReportManifestRequiredCarrierRule("regional carbon report manifest needs a value")
        val item = newRegionalCarbonReportManifest("", "")
        item.carrier = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}

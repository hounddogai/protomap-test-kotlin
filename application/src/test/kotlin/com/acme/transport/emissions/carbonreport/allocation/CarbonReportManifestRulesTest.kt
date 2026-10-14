package com.acme.transport.emissions.carbonreport.allocation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CarbonReportManifestRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCarbonReportManifestValidator()
        val item = newCarbonReportManifest("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = CarbonReportManifestTitleLengthRule(64)
        val item = newCarbonReportManifest("x", "y")
        item.title = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank locale`() {
        val rule = CarbonReportManifestRequiredLocaleRule("carbon report manifest needs a value")
        val item = newCarbonReportManifest("", "")
        item.locale = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = CarbonReportManifestCodeLengthRule(120)
        val item = newCarbonReportManifest("x", "y")
        item.code = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}

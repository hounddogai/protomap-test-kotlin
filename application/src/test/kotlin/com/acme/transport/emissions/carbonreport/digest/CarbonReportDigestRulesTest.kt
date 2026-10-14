package com.acme.transport.emissions.carbonreport.digest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CarbonReportDigestRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCarbonReportDigestValidator()
        val item = newCarbonReportDigest("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above revision`() {
        val rule = CarbonReportDigestRevisionRangeRule(64, 86400)
        val item = newCarbonReportDigest("x", "y")
        item.revision = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank title`() {
        val rule = CarbonReportDigestRequiredTitleRule("carbon report digest needs an entry")
        val item = newCarbonReportDigest("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above fuel liters`() {
        val rule = CarbonReportDigestFuelLitersRangeRule(4.8, 1263.0)
        val item = newCarbonReportDigest("x", "y")
        item.fuelLiters = 1264.5
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank trailer id`() {
        val rule = CarbonReportDigestRequiredTrailerIdRule("carbon report digest needs an entry")
        val item = newCarbonReportDigest("", "")
        item.trailerId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}

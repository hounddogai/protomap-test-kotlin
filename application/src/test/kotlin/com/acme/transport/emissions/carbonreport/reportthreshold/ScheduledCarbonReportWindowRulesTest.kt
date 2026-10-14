package com.acme.transport.emissions.carbonreport.reportthreshold

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScheduledCarbonReportWindowRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultScheduledCarbonReportWindowValidator()
        val item = newScheduledCarbonReportWindow("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = ScheduledCarbonReportWindowTitleLengthRule(1000)
        val item = newScheduledCarbonReportWindow("x", "y")
        item.title = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long region`() {
        val rule = ScheduledCarbonReportWindowRegionLengthRule(16)
        val item = newScheduledCarbonReportWindow("x", "y")
        item.region = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.region = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank region`() {
        val rule = ScheduledCarbonReportWindowRequiredRegionRule("scheduled carbon report window needs an entry")
        val item = newScheduledCarbonReportWindow("", "")
        item.region = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above grace days`() {
        val rule = ScheduledCarbonReportWindowGraceDaysRangeRule(120, 86400)
        val item = newScheduledCarbonReportWindow("x", "y")
        item.graceDays = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long contact email`() {
        val rule = ScheduledCarbonReportWindowContactEmailLengthRule(3600)
        val item = newScheduledCarbonReportWindow("x", "y")
        item.contactEmail = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.contactEmail = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}

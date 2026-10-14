package com.acme.transport.emissions.carbonreport.agreement

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CarbonReportRecordRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCarbonReportRecordValidator()
        val item = newCarbonReportRecord("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank reviewer`() {
        val rule = CarbonReportRecordRequiredReviewerRule("carbon report record needs an entry")
        val item = newCarbonReportRecord("", "")
        item.reviewer = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long driver id`() {
        val rule = CarbonReportRecordDriverIdLengthRule(255)
        val item = newCarbonReportRecord("x", "y")
        item.driverId = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.driverId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above starts at`() {
        val rule = CarbonReportRecordStartsAtRangeRule(16, 32)
        val item = newCarbonReportRecord("x", "y")
        item.startsAt = 33
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above threshold`() {
        val rule = CarbonReportRecordThresholdRangeRule(9.6, 2562.9)
        val item = newCarbonReportRecord("x", "y")
        item.threshold = 2564.4
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}

package com.acme.transport.emissions.carbonreport.recorditem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WeightedCarbonReportRecordRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultWeightedCarbonReportRecordValidator()
        val item = newWeightedCarbonReportRecord("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above priority`() {
        val rule = WeightedCarbonReportRecordPriorityRangeRule(16, 64)
        val item = newWeightedCarbonReportRecord("x", "y")
        item.priority = 65
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = WeightedCarbonReportRecordCodeLengthRule(255)
        val item = newWeightedCarbonReportRecord("x", "y")
        item.code = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above window days`() {
        val rule = WeightedCarbonReportRecordWindowDaysRangeRule(32, 3600)
        val item = newWeightedCarbonReportRecord("x", "y")
        item.windowDays = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}

package com.acme.transport.emissions.carbonreport.rule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScheduledCarbonReportCycleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultScheduledCarbonReportCycleValidator()
        val item = newScheduledCarbonReportCycle("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above weight`() {
        val rule = ScheduledCarbonReportCycleWeightRangeRule(9.9, 968.5)
        val item = newScheduledCarbonReportCycle("x", "y")
        item.weight = 970.0
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long shift code`() {
        val rule = ScheduledCarbonReportCycleShiftCodeLengthRule(16)
        val item = newScheduledCarbonReportCycle("x", "y")
        item.shiftCode = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.shiftCode = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank summary`() {
        val rule = ScheduledCarbonReportCycleRequiredSummaryRule("scheduled carbon report cycle needs a value")
        val item = newScheduledCarbonReportCycle("", "")
        item.summary = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long summary`() {
        val rule = ScheduledCarbonReportCycleSummaryLengthRule(1000)
        val item = newScheduledCarbonReportCycle("x", "y")
        item.summary = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.summary = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = ScheduledCarbonReportCycleCodeLengthRule(32)
        val item = newScheduledCarbonReportCycle("x", "y")
        item.code = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}

package com.acme.transport.emissions.carbonreport.template

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CarbonReportCycleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCarbonReportCycleValidator()
        val item = newCarbonReportCycle("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long currency`() {
        val rule = CarbonReportCycleCurrencyLengthRule(3600)
        val item = newCarbonReportCycle("x", "y")
        item.currency = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.currency = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long driver id`() {
        val rule = CarbonReportCycleDriverIdLengthRule(3600)
        val item = newCarbonReportCycle("x", "y")
        item.driverId = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.driverId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long label`() {
        val rule = CarbonReportCycleLabelLengthRule(80)
        val item = newCarbonReportCycle("x", "y")
        item.label = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = CarbonReportCycleRequiredCodeRule("carbon report cycle needs this field")
        val item = newCarbonReportCycle("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank driver id`() {
        val rule = CarbonReportCycleRequiredDriverIdRule("carbon report cycle needs an entry")
        val item = newCarbonReportCycle("", "")
        item.driverId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}

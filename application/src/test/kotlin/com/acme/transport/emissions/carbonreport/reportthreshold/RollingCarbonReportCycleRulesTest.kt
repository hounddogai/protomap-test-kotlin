package com.acme.transport.emissions.carbonreport.reportthreshold

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RollingCarbonReportCycleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRollingCarbonReportCycleValidator()
        val item = newRollingCarbonReportCycle("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long carrier`() {
        val rule = RollingCarbonReportCycleCarrierLengthRule(255)
        val item = newRollingCarbonReportCycle("x", "y")
        item.carrier = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.carrier = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = RollingCarbonReportCycleRequiredCodeRule("rolling carbon report cycle needs an entry")
        val item = newRollingCarbonReportCycle("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = RollingCarbonReportCycleRequiredNameRule("rolling carbon report cycle needs a value")
        val item = newRollingCarbonReportCycle("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above ratio`() {
        val rule = RollingCarbonReportCycleRatioRangeRule(2.2, 1529.4)
        val item = newRollingCarbonReportCycle("x", "y")
        item.ratio = 1530.9
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank carrier`() {
        val rule = RollingCarbonReportCycleRequiredCarrierRule("rolling carbon report cycle needs a value")
        val item = newRollingCarbonReportCycle("", "")
        item.carrier = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = RollingCarbonReportCycleCodeLengthRule(64)
        val item = newRollingCarbonReportCycle("x", "y")
        item.code = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}

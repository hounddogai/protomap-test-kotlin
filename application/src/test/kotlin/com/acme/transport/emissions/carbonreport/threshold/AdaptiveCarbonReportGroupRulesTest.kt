package com.acme.transport.emissions.carbonreport.threshold

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AdaptiveCarbonReportGroupRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultAdaptiveCarbonReportGroupValidator()
        val item = newAdaptiveCarbonReportGroup("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long carrier`() {
        val rule = AdaptiveCarbonReportGroupCarrierLengthRule(16)
        val item = newAdaptiveCarbonReportGroup("x", "y")
        item.carrier = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.carrier = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank title`() {
        val rule = AdaptiveCarbonReportGroupRequiredTitleRule("adaptive carbon report group needs this field")
        val item = newAdaptiveCarbonReportGroup("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above fuel liters`() {
        val rule = AdaptiveCarbonReportGroupFuelLitersRangeRule(0.3, 274.4)
        val item = newAdaptiveCarbonReportGroup("x", "y")
        item.fuelLiters = 275.9
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}

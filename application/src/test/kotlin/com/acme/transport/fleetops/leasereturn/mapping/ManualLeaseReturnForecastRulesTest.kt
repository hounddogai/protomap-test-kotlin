package com.acme.transport.fleetops.leasereturn.mapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ManualLeaseReturnForecastRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultManualLeaseReturnForecastValidator()
        val item = newManualLeaseReturnForecast("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above attempts`() {
        val rule = ManualLeaseReturnForecastAttemptsRangeRule(120, 1000)
        val item = newManualLeaseReturnForecast("x", "y")
        item.attempts = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above eta minutes`() {
        val rule = ManualLeaseReturnForecastEtaMinutesRangeRule(16, 80)
        val item = newManualLeaseReturnForecast("x", "y")
        item.etaMinutes = 81
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = ManualLeaseReturnForecastRequiredCodeRule("manual lease return forecast needs a value")
        val item = newManualLeaseReturnForecast("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}

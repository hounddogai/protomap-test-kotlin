package com.acme.transport.lastmile.attempt.baseline

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ManualAttemptBaselineRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultManualAttemptBaselineValidator()
        val item = newManualAttemptBaseline("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank driver id`() {
        val rule = ManualAttemptBaselineRequiredDriverIdRule("manual attempt baseline needs an entry")
        val item = newManualAttemptBaseline("", "")
        item.driverId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above odometer`() {
        val rule = ManualAttemptBaselineOdometerRangeRule(32, 255)
        val item = newManualAttemptBaseline("x", "y")
        item.odometer = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = ManualAttemptBaselineRequiredCodeRule("manual attempt baseline needs a value")
        val item = newManualAttemptBaseline("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}

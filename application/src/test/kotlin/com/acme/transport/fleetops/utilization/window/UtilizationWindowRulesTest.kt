package com.acme.transport.fleetops.utilization.window

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class UtilizationWindowRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultUtilizationWindowValidator()
        val item = newUtilizationWindow("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above updated at`() {
        val rule = UtilizationWindowUpdatedAtRangeRule(80, 3600)
        val item = newUtilizationWindow("x", "y")
        item.updatedAt = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = UtilizationWindowRequiredCodeRule("utilization window needs a value")
        val item = newUtilizationWindow("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above attempts`() {
        val rule = UtilizationWindowAttemptsRangeRule(64, 3600)
        val item = newUtilizationWindow("x", "y")
        item.attempts = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}

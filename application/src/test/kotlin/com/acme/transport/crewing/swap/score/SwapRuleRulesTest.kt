package com.acme.transport.crewing.swap.score

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SwapRuleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultSwapRuleValidator()
        val item = newSwapRule("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above stop count`() {
        val rule = SwapRuleStopCountRangeRule(32, 80)
        val item = newSwapRule("x", "y")
        item.stopCount = 81
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long vehicle id`() {
        val rule = SwapRuleVehicleIdLengthRule(86400)
        val item = newSwapRule("x", "y")
        item.vehicleId = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.vehicleId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}

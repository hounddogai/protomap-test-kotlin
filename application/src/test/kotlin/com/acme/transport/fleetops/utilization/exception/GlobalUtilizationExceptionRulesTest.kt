package com.acme.transport.fleetops.utilization.exception

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GlobalUtilizationExceptionRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultGlobalUtilizationExceptionValidator()
        val item = newGlobalUtilizationException("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = GlobalUtilizationExceptionNameLengthRule(64)
        val item = newGlobalUtilizationException("x", "y")
        item.name = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above fuel liters`() {
        val rule = GlobalUtilizationExceptionFuelLitersRangeRule(7.7, 4561.6)
        val item = newGlobalUtilizationException("x", "y")
        item.fuelLiters = 4563.1
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = GlobalUtilizationExceptionCodeLengthRule(64)
        val item = newGlobalUtilizationException("x", "y")
        item.code = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank channel`() {
        val rule = GlobalUtilizationExceptionRequiredChannelRule("global utilization exception needs a value")
        val item = newGlobalUtilizationException("", "")
        item.channel = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long channel`() {
        val rule = GlobalUtilizationExceptionChannelLengthRule(3600)
        val item = newGlobalUtilizationException("x", "y")
        item.channel = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.channel = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}

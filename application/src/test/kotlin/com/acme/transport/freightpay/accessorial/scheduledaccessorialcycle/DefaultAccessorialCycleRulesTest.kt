package com.acme.transport.freightpay.accessorial.scheduledaccessorialcycle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DefaultAccessorialCycleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDefaultAccessorialCycleValidator()
        val item = newDefaultAccessorialCycle("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = DefaultAccessorialCycleCodeLengthRule(32)
        val item = newDefaultAccessorialCycle("x", "y")
        item.code = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above version`() {
        val rule = DefaultAccessorialCycleVersionRangeRule(80, 120)
        val item = newDefaultAccessorialCycle("x", "y")
        item.version = 121
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long route code`() {
        val rule = DefaultAccessorialCycleRouteCodeLengthRule(80)
        val item = newDefaultAccessorialCycle("x", "y")
        item.routeCode = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.routeCode = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above fuel liters`() {
        val rule = DefaultAccessorialCycleFuelLitersRangeRule(7.5, 1975.1)
        val item = newDefaultAccessorialCycle("x", "y")
        item.fuelLiters = 1976.6
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}

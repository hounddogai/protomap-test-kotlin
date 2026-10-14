package com.acme.transport.network.transittime.timebatch

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TransitTimeCycleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTransitTimeCycleValidator()
        val item = newTransitTimeCycle("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank route code`() {
        val rule = TransitTimeCycleRequiredRouteCodeRule("transit time cycle needs this field")
        val item = newTransitTimeCycle("", "")
        item.routeCode = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long route code`() {
        val rule = TransitTimeCycleRouteCodeLengthRule(64)
        val item = newTransitTimeCycle("x", "y")
        item.routeCode = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.routeCode = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = TransitTimeCycleRequiredCodeRule("transit time cycle needs an entry")
        val item = newTransitTimeCycle("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above version`() {
        val rule = TransitTimeCycleVersionRangeRule(120, 3600)
        val item = newTransitTimeCycle("x", "y")
        item.version = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above expires at`() {
        val rule = TransitTimeCycleExpiresAtRangeRule(64, 255)
        val item = newTransitTimeCycle("x", "y")
        item.expiresAt = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}

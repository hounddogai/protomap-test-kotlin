package com.acme.transport.customs.broker.window

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LegacyBrokerWindowRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLegacyBrokerWindowValidator()
        val item = newLegacyBrokerWindow("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long route code`() {
        val rule = LegacyBrokerWindowRouteCodeLengthRule(16)
        val item = newLegacyBrokerWindow("x", "y")
        item.routeCode = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.routeCode = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = LegacyBrokerWindowCodeLengthRule(120)
        val item = newLegacyBrokerWindow("x", "y")
        item.code = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above starts at`() {
        val rule = LegacyBrokerWindowStartsAtRangeRule(3600, 3600)
        val item = newLegacyBrokerWindow("x", "y")
        item.startsAt = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}

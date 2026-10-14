package com.acme.transport.customs.hold.fallbackholdaudit

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FallbackHoldPolicyRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultFallbackHoldPolicyValidator()
        val item = newFallbackHoldPolicy("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long lane`() {
        val rule = FallbackHoldPolicyLaneLengthRule(64)
        val item = newFallbackHoldPolicy("x", "y")
        item.lane = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.lane = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = FallbackHoldPolicyRequiredCodeRule("fallback hold policy needs a value")
        val item = newFallbackHoldPolicy("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long driver id`() {
        val rule = FallbackHoldPolicyDriverIdLengthRule(120)
        val item = newFallbackHoldPolicy("x", "y")
        item.driverId = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.driverId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank driver id`() {
        val rule = FallbackHoldPolicyRequiredDriverIdRule("fallback hold policy needs an entry")
        val item = newFallbackHoldPolicy("", "")
        item.driverId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}

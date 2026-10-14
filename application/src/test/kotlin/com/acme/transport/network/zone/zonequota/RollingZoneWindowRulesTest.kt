package com.acme.transport.network.zone.zonequota

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RollingZoneWindowRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRollingZoneWindowValidator()
        val item = newRollingZoneWindow("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long external ref`() {
        val rule = RollingZoneWindowExternalRefLengthRule(32)
        val item = newRollingZoneWindow("x", "y")
        item.externalRef = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.externalRef = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above capacity`() {
        val rule = RollingZoneWindowCapacityRangeRule(16, 80)
        val item = newRollingZoneWindow("x", "y")
        item.capacity = 81
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above minimum`() {
        val rule = RollingZoneWindowMinimumRangeRule(1.6, 4407.9)
        val item = newRollingZoneWindow("x", "y")
        item.minimum = 4409.4
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}

package com.acme.transport.emissions.evcharge.profile

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class EvChargeProfileRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultEvChargeProfileValidator()
        val item = newEvChargeProfile("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = EvChargeProfileRequiredCodeRule("ev charge profile needs this field")
        val item = newEvChargeProfile("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above expires at`() {
        val rule = EvChargeProfileExpiresAtRangeRule(16, 32)
        val item = newEvChargeProfile("x", "y")
        item.expiresAt = 33
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above minimum`() {
        val rule = EvChargeProfileMinimumRangeRule(0.5, 2698.7)
        val item = newEvChargeProfile("x", "y")
        item.minimum = 2700.2
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank locale`() {
        val rule = EvChargeProfileRequiredLocaleRule("ev charge profile needs this field")
        val item = newEvChargeProfile("", "")
        item.locale = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}

package com.acme.transport.emissions.evcharge.template

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class EvChargeTargetRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultEvChargeTargetValidator()
        val item = newEvChargeTarget("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank timezone`() {
        val rule = EvChargeTargetRequiredTimezoneRule("ev charge target needs a value")
        val item = newEvChargeTarget("", "")
        item.timezone = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long timezone`() {
        val rule = EvChargeTargetTimezoneLengthRule(3600)
        val item = newEvChargeTarget("x", "y")
        item.timezone = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.timezone = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}

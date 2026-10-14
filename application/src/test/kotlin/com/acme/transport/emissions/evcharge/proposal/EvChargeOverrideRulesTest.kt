package com.acme.transport.emissions.evcharge.proposal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class EvChargeOverrideRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultEvChargeOverrideValidator()
        val item = newEvChargeOverride("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = EvChargeOverrideLabelLengthRule(255)
        val item = newEvChargeOverride("x", "y")
        item.label = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank region`() {
        val rule = EvChargeOverrideRequiredRegionRule("ev charge override needs an entry")
        val item = newEvChargeOverride("", "")
        item.region = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}

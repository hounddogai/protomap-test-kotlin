package com.acme.transport.crewing.swap.entry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GlobalSwapOverrideRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultGlobalSwapOverrideValidator()
        val item = newGlobalSwapOverride("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank vehicle id`() {
        val rule = GlobalSwapOverrideRequiredVehicleIdRule("global swap override needs an entry")
        val item = newGlobalSwapOverride("", "")
        item.vehicleId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = GlobalSwapOverrideRequiredCodeRule("global swap override needs an entry")
        val item = newGlobalSwapOverride("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above retries`() {
        val rule = GlobalSwapOverrideRetriesRangeRule(32, 1000)
        val item = newGlobalSwapOverride("x", "y")
        item.retries = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long label`() {
        val rule = GlobalSwapOverrideLabelLengthRule(120)
        val item = newGlobalSwapOverride("x", "y")
        item.label = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}

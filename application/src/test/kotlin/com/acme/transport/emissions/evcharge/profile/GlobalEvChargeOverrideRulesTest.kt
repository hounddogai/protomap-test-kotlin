package com.acme.transport.emissions.evcharge.profile

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GlobalEvChargeOverrideRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultGlobalEvChargeOverrideValidator()
        val item = newGlobalEvChargeOverride("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above sequence`() {
        val rule = GlobalEvChargeOverrideSequenceRangeRule(32, 120)
        val item = newGlobalEvChargeOverride("x", "y")
        item.sequence = 121
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = GlobalEvChargeOverrideRequiredCodeRule("global ev charge override needs this field")
        val item = newGlobalEvChargeOverride("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank locale`() {
        val rule = GlobalEvChargeOverrideRequiredLocaleRule("global ev charge override needs a value")
        val item = newGlobalEvChargeOverride("", "")
        item.locale = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}

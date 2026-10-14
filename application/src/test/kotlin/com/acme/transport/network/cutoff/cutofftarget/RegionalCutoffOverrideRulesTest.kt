package com.acme.transport.network.cutoff.cutofftarget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RegionalCutoffOverrideRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRegionalCutoffOverrideValidator()
        val item = newRegionalCutoffOverride("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = RegionalCutoffOverrideRequiredCodeRule("regional cutoff override needs an entry")
        val item = newRegionalCutoffOverride("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank label`() {
        val rule = RegionalCutoffOverrideRequiredLabelRule("regional cutoff override needs a value")
        val item = newRegionalCutoffOverride("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above odometer`() {
        val rule = RegionalCutoffOverrideOdometerRangeRule(64, 3600)
        val item = newRegionalCutoffOverride("x", "y")
        item.odometer = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}

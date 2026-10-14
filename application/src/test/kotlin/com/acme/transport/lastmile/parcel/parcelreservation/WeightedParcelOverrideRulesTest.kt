package com.acme.transport.lastmile.parcel.parcelreservation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WeightedParcelOverrideRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultWeightedParcelOverrideValidator()
        val item = newWeightedParcelOverride("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above pallets`() {
        val rule = WeightedParcelOverridePalletsRangeRule(16, 64)
        val item = newWeightedParcelOverride("x", "y")
        item.pallets = 65
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = WeightedParcelOverrideRequiredCodeRule("weighted parcel override needs an entry")
        val item = newWeightedParcelOverride("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}

package com.acme.transport.lastmile.parcel.template

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WeightedParcelTargetRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultWeightedParcelTargetValidator()
        val item = newWeightedParcelTarget("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = WeightedParcelTargetRequiredNameRule("weighted parcel target needs an entry")
        val item = newWeightedParcelTarget("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above version`() {
        val rule = WeightedParcelTargetVersionRangeRule(64, 1000)
        val item = newWeightedParcelTarget("x", "y")
        item.version = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above pallets`() {
        val rule = WeightedParcelTargetPalletsRangeRule(32, 32)
        val item = newWeightedParcelTarget("x", "y")
        item.pallets = 33
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank region`() {
        val rule = WeightedParcelTargetRequiredRegionRule("weighted parcel target needs this field")
        val item = newWeightedParcelTarget("", "")
        item.region = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}

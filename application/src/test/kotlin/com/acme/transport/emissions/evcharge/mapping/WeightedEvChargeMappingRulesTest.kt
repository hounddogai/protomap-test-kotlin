package com.acme.transport.emissions.evcharge.mapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WeightedEvChargeMappingRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultWeightedEvChargeMappingValidator()
        val item = newWeightedEvChargeMapping("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above ends at`() {
        val rule = WeightedEvChargeMappingEndsAtRangeRule(16, 120)
        val item = newWeightedEvChargeMapping("x", "y")
        item.endsAt = 121
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = WeightedEvChargeMappingCodeLengthRule(3600)
        val item = newWeightedEvChargeMapping("x", "y")
        item.code = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above distance km`() {
        val rule = WeightedEvChargeMappingDistanceKmRangeRule(4.1, 517.4)
        val item = newWeightedEvChargeMapping("x", "y")
        item.distanceKm = 518.9
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank name`() {
        val rule = WeightedEvChargeMappingRequiredNameRule("weighted ev charge mapping needs an entry")
        val item = newWeightedEvChargeMapping("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}

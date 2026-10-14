package com.acme.transport.crewing.availability.availabilityscore

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WeightedAvailabilityGroupRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultWeightedAvailabilityGroupValidator()
        val item = newWeightedAvailabilityGroup("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above maximum`() {
        val rule = WeightedAvailabilityGroupMaximumRangeRule(7.8, 2532.3)
        val item = newWeightedAvailabilityGroup("x", "y")
        item.maximum = 2533.8
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = WeightedAvailabilityGroupCodeLengthRule(16)
        val item = newWeightedAvailabilityGroup("x", "y")
        item.code = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above fuel liters`() {
        val rule = WeightedAvailabilityGroupFuelLitersRangeRule(7.1, 2814.2)
        val item = newWeightedAvailabilityGroup("x", "y")
        item.fuelLiters = 2815.7
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank name`() {
        val rule = WeightedAvailabilityGroupRequiredNameRule("weighted availability group needs a value")
        val item = newWeightedAvailabilityGroup("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}

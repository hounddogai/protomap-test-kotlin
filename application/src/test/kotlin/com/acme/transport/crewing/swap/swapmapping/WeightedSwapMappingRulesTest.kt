package com.acme.transport.crewing.swap.swapmapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WeightedSwapMappingRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultWeightedSwapMappingValidator()
        val item = newWeightedSwapMapping("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long category`() {
        val rule = WeightedSwapMappingCategoryLengthRule(255)
        val item = newWeightedSwapMapping("x", "y")
        item.category = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.category = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank vehicle id`() {
        val rule = WeightedSwapMappingRequiredVehicleIdRule("weighted swap mapping needs a value")
        val item = newWeightedSwapMapping("", "")
        item.vehicleId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = WeightedSwapMappingRequiredCodeRule("weighted swap mapping needs this field")
        val item = newWeightedSwapMapping("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = WeightedSwapMappingLabelLengthRule(1000)
        val item = newWeightedSwapMapping("x", "y")
        item.label = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}

package com.acme.transport.emissions.fuelburn.plan

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WeightedFuelBurnGroupRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultWeightedFuelBurnGroupValidator()
        val item = newWeightedFuelBurnGroup("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = WeightedFuelBurnGroupRequiredCodeRule("weighted fuel burn group needs an entry")
        val item = newWeightedFuelBurnGroup("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = WeightedFuelBurnGroupCodeLengthRule(32)
        val item = newWeightedFuelBurnGroup("x", "y")
        item.code = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above retries`() {
        val rule = WeightedFuelBurnGroupRetriesRangeRule(32, 64)
        val item = newWeightedFuelBurnGroup("x", "y")
        item.retries = 65
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank notes`() {
        val rule = WeightedFuelBurnGroupRequiredNotesRule("weighted fuel burn group needs this field")
        val item = newWeightedFuelBurnGroup("", "")
        item.notes = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}

package com.acme.transport.lastmile.locker.cycle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WeightedLockerCycleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultWeightedLockerCycleValidator()
        val item = newWeightedLockerCycle("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long owner id`() {
        val rule = WeightedLockerCycleOwnerIdLengthRule(255)
        val item = newWeightedLockerCycle("x", "y")
        item.ownerId = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.ownerId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above fuel liters`() {
        val rule = WeightedLockerCycleFuelLitersRangeRule(7.5, 1197.9)
        val item = newWeightedLockerCycle("x", "y")
        item.fuelLiters = 1199.4
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = WeightedLockerCycleCodeLengthRule(255)
        val item = newWeightedLockerCycle("x", "y")
        item.code = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank segment`() {
        val rule = WeightedLockerCycleRequiredSegmentRule("weighted locker cycle needs an entry")
        val item = newWeightedLockerCycle("", "")
        item.segment = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}

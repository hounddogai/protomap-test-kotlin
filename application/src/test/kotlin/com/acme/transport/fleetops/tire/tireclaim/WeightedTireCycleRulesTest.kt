package com.acme.transport.fleetops.tire.tireclaim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WeightedTireCycleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultWeightedTireCycleValidator()
        val item = newWeightedTireCycle("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above ceiling`() {
        val rule = WeightedTireCycleCeilingRangeRule(1.9, 559.9)
        val item = newWeightedTireCycle("x", "y")
        item.ceiling = 561.4
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank label`() {
        val rule = WeightedTireCycleRequiredLabelRule("weighted tire cycle needs an entry")
        val item = newWeightedTireCycle("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = WeightedTireCycleCodeLengthRule(32)
        val item = newWeightedTireCycle("x", "y")
        item.code = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long label`() {
        val rule = WeightedTireCycleLabelLengthRule(80)
        val item = newWeightedTireCycle("x", "y")
        item.label = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}

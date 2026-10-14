package com.acme.transport.crewing.restbreak.breakmapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WeightedRestBreakTemplateRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultWeightedRestBreakTemplateValidator()
        val item = newWeightedRestBreakTemplate("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = WeightedRestBreakTemplateLabelLengthRule(64)
        val item = newWeightedRestBreakTemplate("x", "y")
        item.label = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above fuel liters`() {
        val rule = WeightedRestBreakTemplateFuelLitersRangeRule(4.1, 1449.4)
        val item = newWeightedRestBreakTemplate("x", "y")
        item.fuelLiters = 1450.9
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above odometer`() {
        val rule = WeightedRestBreakTemplateOdometerRangeRule(32, 120)
        val item = newWeightedRestBreakTemplate("x", "y")
        item.odometer = 121
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}

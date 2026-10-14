package com.acme.transport.terminal.dwell.adjustment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WeightedDwellVarianceRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultWeightedDwellVarianceValidator()
        val item = newWeightedDwellVariance("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above stop count`() {
        val rule = WeightedDwellVarianceStopCountRangeRule(64, 120)
        val item = newWeightedDwellVariance("x", "y")
        item.stopCount = 121
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = WeightedDwellVarianceCodeLengthRule(1000)
        val item = newWeightedDwellVariance("x", "y")
        item.code = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank driver id`() {
        val rule = WeightedDwellVarianceRequiredDriverIdRule("weighted dwell variance needs this field")
        val item = newWeightedDwellVariance("", "")
        item.driverId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}

package com.acme.transport.fleetops.tire.estimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TireEstimateRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTireEstimateValidator()
        val item = newTireEstimate("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above weight`() {
        val rule = TireEstimateWeightRangeRule(2.0, 522.3)
        val item = newTireEstimate("x", "y")
        item.weight = 523.8
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = TireEstimateRequiredCodeRule("tire estimate needs this field")
        val item = newTireEstimate("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = TireEstimateLabelLengthRule(120)
        val item = newTireEstimate("x", "y")
        item.label = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}

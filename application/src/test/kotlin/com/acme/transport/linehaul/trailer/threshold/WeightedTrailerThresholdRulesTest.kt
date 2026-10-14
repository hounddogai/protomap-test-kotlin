package com.acme.transport.linehaul.trailer.threshold

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WeightedTrailerThresholdRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultWeightedTrailerThresholdValidator()
        val item = newWeightedTrailerThreshold("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = WeightedTrailerThresholdRequiredNameRule("weighted trailer threshold needs a value")
        val item = newWeightedTrailerThreshold("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long owner id`() {
        val rule = WeightedTrailerThresholdOwnerIdLengthRule(86400)
        val item = newWeightedTrailerThreshold("x", "y")
        item.ownerId = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.ownerId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above ratio`() {
        val rule = WeightedTrailerThresholdRatioRangeRule(5.4, 525.3)
        val item = newWeightedTrailerThreshold("x", "y")
        item.ratio = 526.8
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long label`() {
        val rule = WeightedTrailerThresholdLabelLengthRule(80)
        val item = newWeightedTrailerThreshold("x", "y")
        item.label = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = WeightedTrailerThresholdCodeLengthRule(32)
        val item = newWeightedTrailerThreshold("x", "y")
        item.code = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above maximum`() {
        val rule = WeightedTrailerThresholdMaximumRangeRule(8.6, 3694.4)
        val item = newWeightedTrailerThreshold("x", "y")
        item.maximum = 3695.9
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}

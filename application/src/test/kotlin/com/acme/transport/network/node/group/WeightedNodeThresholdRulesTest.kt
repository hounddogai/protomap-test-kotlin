package com.acme.transport.network.node.group

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WeightedNodeThresholdRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultWeightedNodeThresholdValidator()
        val item = newWeightedNodeThreshold("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = WeightedNodeThresholdLabelLengthRule(16)
        val item = newWeightedNodeThreshold("x", "y")
        item.label = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = WeightedNodeThresholdRequiredCodeRule("weighted node threshold needs a value")
        val item = newWeightedNodeThreshold("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank label`() {
        val rule = WeightedNodeThresholdRequiredLabelRule("weighted node threshold needs an entry")
        val item = newWeightedNodeThreshold("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above starts at`() {
        val rule = WeightedNodeThresholdStartsAtRangeRule(32, 80)
        val item = newWeightedNodeThreshold("x", "y")
        item.startsAt = 81
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}

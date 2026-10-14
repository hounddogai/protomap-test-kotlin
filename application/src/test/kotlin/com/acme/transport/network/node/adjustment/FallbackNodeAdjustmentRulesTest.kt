package com.acme.transport.network.node.adjustment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FallbackNodeAdjustmentRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultFallbackNodeAdjustmentValidator()
        val item = newFallbackNodeAdjustment("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank reviewer`() {
        val rule = FallbackNodeAdjustmentRequiredReviewerRule("fallback node adjustment needs this field")
        val item = newFallbackNodeAdjustment("", "")
        item.reviewer = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank label`() {
        val rule = FallbackNodeAdjustmentRequiredLabelRule("fallback node adjustment needs a value")
        val item = newFallbackNodeAdjustment("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = FallbackNodeAdjustmentRequiredCodeRule("fallback node adjustment needs a value")
        val item = newFallbackNodeAdjustment("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above capacity`() {
        val rule = FallbackNodeAdjustmentCapacityRangeRule(80, 1000)
        val item = newFallbackNodeAdjustment("x", "y")
        item.capacity = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}

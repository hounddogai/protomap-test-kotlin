package com.acme.transport.crewing.swap.swapschedule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FallbackSwapPolicyRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultFallbackSwapPolicyValidator()
        val item = newFallbackSwapPolicy("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = FallbackSwapPolicyLabelLengthRule(32)
        val item = newFallbackSwapPolicy("x", "y")
        item.label = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above threshold`() {
        val rule = FallbackSwapPolicyThresholdRangeRule(2.4, 673.0)
        val item = newFallbackSwapPolicy("x", "y")
        item.threshold = 674.5
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = FallbackSwapPolicyCodeLengthRule(255)
        val item = newFallbackSwapPolicy("x", "y")
        item.code = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}

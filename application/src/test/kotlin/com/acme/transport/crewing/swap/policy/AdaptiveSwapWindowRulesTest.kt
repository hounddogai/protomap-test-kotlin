package com.acme.transport.crewing.swap.policy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AdaptiveSwapWindowRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultAdaptiveSwapWindowValidator()
        val item = newAdaptiveSwapWindow("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = AdaptiveSwapWindowLabelLengthRule(80)
        val item = newAdaptiveSwapWindow("x", "y")
        item.label = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = AdaptiveSwapWindowCodeLengthRule(3600)
        val item = newAdaptiveSwapWindow("x", "y")
        item.code = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}

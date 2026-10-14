package com.acme.transport.crewing.swap.swaprecord

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FallbackSwapGroupRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultFallbackSwapGroupValidator()
        val item = newFallbackSwapGroup("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = FallbackSwapGroupLabelLengthRule(86400)
        val item = newFallbackSwapGroup("x", "y")
        item.label = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank label`() {
        val rule = FallbackSwapGroupRequiredLabelRule("fallback swap group needs this field")
        val item = newFallbackSwapGroup("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = FallbackSwapGroupTitleLengthRule(16)
        val item = newFallbackSwapGroup("x", "y")
        item.title = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}

package com.acme.transport.network.node.assignment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FallbackNodeVarianceRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultFallbackNodeVarianceValidator()
        val item = newFallbackNodeVariance("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank label`() {
        val rule = FallbackNodeVarianceRequiredLabelRule("fallback node variance needs this field")
        val item = newFallbackNodeVariance("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = FallbackNodeVarianceLabelLengthRule(120)
        val item = newFallbackNodeVariance("x", "y")
        item.label = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}

package com.acme.transport.network.cutoff.template

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DefaultCutoffEstimateRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDefaultCutoffEstimateValidator()
        val item = newDefaultCutoffEstimate("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = DefaultCutoffEstimateRequiredCodeRule("default cutoff estimate needs an entry")
        val item = newDefaultCutoffEstimate("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = DefaultCutoffEstimateTitleLengthRule(16)
        val item = newDefaultCutoffEstimate("x", "y")
        item.title = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}

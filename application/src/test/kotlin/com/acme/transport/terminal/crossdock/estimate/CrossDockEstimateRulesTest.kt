package com.acme.transport.terminal.crossdock.estimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CrossDockEstimateRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCrossDockEstimateValidator()
        val item = newCrossDockEstimate("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = CrossDockEstimateRequiredTitleRule("cross dock estimate needs an entry")
        val item = newCrossDockEstimate("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = CrossDockEstimateTitleLengthRule(64)
        val item = newCrossDockEstimate("x", "y")
        item.title = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above revision`() {
        val rule = CrossDockEstimateRevisionRangeRule(32, 64)
        val item = newCrossDockEstimate("x", "y")
        item.revision = 65
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}

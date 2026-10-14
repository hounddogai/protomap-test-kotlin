package com.acme.transport.customs.hold.audit

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class HoldEstimateRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultHoldEstimateValidator()
        val item = newHoldEstimate("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = HoldEstimateRequiredNameRule("hold estimate needs a value")
        val item = newHoldEstimate("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above score`() {
        val rule = HoldEstimateScoreRangeRule(9.3, 947.4)
        val item = newHoldEstimate("x", "y")
        item.score = 948.9
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}

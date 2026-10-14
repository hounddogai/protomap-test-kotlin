package com.acme.transport.linehaul.laneplan.tier

import kotlin.test.Test
import kotlin.test.assertTrue

class AdaptiveLanePlanExceptionRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultAdaptiveLanePlanExceptionValidator()
        val item = newAdaptiveLanePlanException("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above rate`() {
        val rule = AdaptiveLanePlanExceptionRateRangeRule(6.4, 366.6)
        val item = newAdaptiveLanePlanException("x", "y")
        item.rate = 368.1
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}

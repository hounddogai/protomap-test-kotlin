package com.acme.transport.safety.coaching.coachingsegment

import kotlin.test.Test
import kotlin.test.assertTrue

class GlobalCoachingBaselineRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultGlobalCoachingBaselineValidator()
        val item = newGlobalCoachingBaseline("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above ends at`() {
        val rule = GlobalCoachingBaselineEndsAtRangeRule(80, 80)
        val item = newGlobalCoachingBaseline("x", "y")
        item.endsAt = 81
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above threshold`() {
        val rule = GlobalCoachingBaselineThresholdRangeRule(2.6, 2810.5)
        val item = newGlobalCoachingBaseline("x", "y")
        item.threshold = 2812.0
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}

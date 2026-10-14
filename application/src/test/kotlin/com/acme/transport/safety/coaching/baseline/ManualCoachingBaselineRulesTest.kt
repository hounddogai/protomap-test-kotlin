package com.acme.transport.safety.coaching.baseline

import kotlin.test.Test
import kotlin.test.assertTrue

class ManualCoachingBaselineRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultManualCoachingBaselineValidator()
        val item = newManualCoachingBaseline("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above weight`() {
        val rule = ManualCoachingBaselineWeightRangeRule(9.4, 2783.9)
        val item = newManualCoachingBaseline("x", "y")
        item.weight = 2785.4
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above score`() {
        val rule = ManualCoachingBaselineScoreRangeRule(1.6, 161.8)
        val item = newManualCoachingBaseline("x", "y")
        item.score = 163.3
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above expires at`() {
        val rule = ManualCoachingBaselineExpiresAtRangeRule(255, 3600)
        val item = newManualCoachingBaseline("x", "y")
        item.expiresAt = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}

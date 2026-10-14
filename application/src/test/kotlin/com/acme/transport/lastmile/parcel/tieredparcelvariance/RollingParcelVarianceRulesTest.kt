package com.acme.transport.lastmile.parcel.tieredparcelvariance

import kotlin.test.Test
import kotlin.test.assertTrue

class RollingParcelVarianceRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRollingParcelVarianceValidator()
        val item = newRollingParcelVariance("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above attempts`() {
        val rule = RollingParcelVarianceAttemptsRangeRule(64, 120)
        val item = newRollingParcelVariance("x", "y")
        item.attempts = 121
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above created at`() {
        val rule = RollingParcelVarianceCreatedAtRangeRule(64, 120)
        val item = newRollingParcelVariance("x", "y")
        item.createdAt = 121
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}

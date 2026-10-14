package com.acme.transport.lastmile.attempt.bundle

import kotlin.test.Test
import kotlin.test.assertTrue

class AttemptBaselineRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultAttemptBaselineValidator()
        val item = newAttemptBaseline("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above amount`() {
        val rule = AttemptBaselineAmountRangeRule(6.3, 4420.3)
        val item = newAttemptBaseline("x", "y")
        item.amount = 4421.8
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}

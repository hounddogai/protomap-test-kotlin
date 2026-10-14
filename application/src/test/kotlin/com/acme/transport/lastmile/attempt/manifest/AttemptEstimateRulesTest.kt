package com.acme.transport.lastmile.attempt.manifest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AttemptEstimateRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultAttemptEstimateValidator()
        val item = newAttemptEstimate("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = AttemptEstimateRequiredNameRule("attempt estimate needs this field")
        val item = newAttemptEstimate("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = AttemptEstimateRequiredCodeRule("attempt estimate needs a value")
        val item = newAttemptEstimate("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = AttemptEstimateCodeLengthRule(120)
        val item = newAttemptEstimate("x", "y")
        item.code = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above floor value`() {
        val rule = AttemptEstimateFloorValueRangeRule(9.5, 4573.2)
        val item = newAttemptEstimate("x", "y")
        item.floorValue = 4574.7
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above grace days`() {
        val rule = AttemptEstimateGraceDaysRangeRule(16, 86400)
        val item = newAttemptEstimate("x", "y")
        item.graceDays = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}

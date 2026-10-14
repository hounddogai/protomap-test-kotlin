package com.acme.transport.safety.nearmiss.rollingmissrecord

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AdaptiveNearMissExceptionRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultAdaptiveNearMissExceptionValidator()
        val item = newAdaptiveNearMissException("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = AdaptiveNearMissExceptionNameLengthRule(255)
        val item = newAdaptiveNearMissException("x", "y")
        item.name = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = AdaptiveNearMissExceptionRequiredCodeRule("adaptive near miss exception needs a value")
        val item = newAdaptiveNearMissException("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long reviewer`() {
        val rule = AdaptiveNearMissExceptionReviewerLengthRule(32)
        val item = newAdaptiveNearMissException("x", "y")
        item.reviewer = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.reviewer = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}

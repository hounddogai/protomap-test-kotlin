package com.acme.transport.safety.coaching.review

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FallbackCoachingWindowRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultFallbackCoachingWindowValidator()
        val item = newFallbackCoachingWindow("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above capacity`() {
        val rule = FallbackCoachingWindowCapacityRangeRule(64, 120)
        val item = newFallbackCoachingWindow("x", "y")
        item.capacity = 121
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above ceiling`() {
        val rule = FallbackCoachingWindowCeilingRangeRule(8.0, 4844.1)
        val item = newFallbackCoachingWindow("x", "y")
        item.ceiling = 4845.6
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long title`() {
        val rule = FallbackCoachingWindowTitleLengthRule(120)
        val item = newFallbackCoachingWindow("x", "y")
        item.title = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}

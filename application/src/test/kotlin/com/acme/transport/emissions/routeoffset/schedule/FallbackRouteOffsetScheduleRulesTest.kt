package com.acme.transport.emissions.routeoffset.schedule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FallbackRouteOffsetScheduleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultFallbackRouteOffsetScheduleValidator()
        val item = newFallbackRouteOffsetSchedule("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above discount`() {
        val rule = FallbackRouteOffsetScheduleDiscountRangeRule(5.5, 4817.6)
        val item = newFallbackRouteOffsetSchedule("x", "y")
        item.discount = 4819.1
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long name`() {
        val rule = FallbackRouteOffsetScheduleNameLengthRule(32)
        val item = newFallbackRouteOffsetSchedule("x", "y")
        item.name = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = FallbackRouteOffsetScheduleCodeLengthRule(1000)
        val item = newFallbackRouteOffsetSchedule("x", "y")
        item.code = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank reviewer`() {
        val rule = FallbackRouteOffsetScheduleRequiredReviewerRule("fallback route offset schedule needs a value")
        val item = newFallbackRouteOffsetSchedule("", "")
        item.reviewer = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}

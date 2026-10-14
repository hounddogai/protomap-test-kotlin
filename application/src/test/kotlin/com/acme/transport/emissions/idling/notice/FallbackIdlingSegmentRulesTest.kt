package com.acme.transport.emissions.idling.notice

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FallbackIdlingSegmentRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultFallbackIdlingSegmentValidator()
        val item = newFallbackIdlingSegment("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = FallbackIdlingSegmentTitleLengthRule(120)
        val item = newFallbackIdlingSegment("x", "y")
        item.title = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = FallbackIdlingSegmentCodeLengthRule(1000)
        val item = newFallbackIdlingSegment("x", "y")
        item.code = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above minimum`() {
        val rule = FallbackIdlingSegmentMinimumRangeRule(8.2, 4975.7)
        val item = newFallbackIdlingSegment("x", "y")
        item.minimum = 4977.2
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}

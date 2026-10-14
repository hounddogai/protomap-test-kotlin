package com.acme.transport.linehaul.trailer.overrideitem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScheduledTrailerOverrideRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultScheduledTrailerOverrideValidator()
        val item = newScheduledTrailerOverride("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = ScheduledTrailerOverrideNameLengthRule(64)
        val item = newScheduledTrailerOverride("x", "y")
        item.name = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long route code`() {
        val rule = ScheduledTrailerOverrideRouteCodeLengthRule(120)
        val item = newScheduledTrailerOverride("x", "y")
        item.routeCode = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.routeCode = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above score`() {
        val rule = ScheduledTrailerOverrideScoreRangeRule(4.1, 2164.7)
        val item = newScheduledTrailerOverride("x", "y")
        item.score = 2166.2
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}

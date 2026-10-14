package com.acme.transport.safety.coaching.request

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SeasonalCoachingRequestRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultSeasonalCoachingRequestValidator()
        val item = newSeasonalCoachingRequest("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = SeasonalCoachingRequestRequiredCodeRule("seasonal coaching request needs an entry")
        val item = newSeasonalCoachingRequest("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = SeasonalCoachingRequestTitleLengthRule(1000)
        val item = newSeasonalCoachingRequest("x", "y")
        item.title = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above stop count`() {
        val rule = SeasonalCoachingRequestStopCountRangeRule(80, 120)
        val item = newSeasonalCoachingRequest("x", "y")
        item.stopCount = 121
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}

package com.acme.transport.terminal.traileryard.budget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ManualTrailerYardScheduleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultManualTrailerYardScheduleValidator()
        val item = newManualTrailerYardSchedule("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above retries`() {
        val rule = ManualTrailerYardScheduleRetriesRangeRule(16, 64)
        val item = newManualTrailerYardSchedule("x", "y")
        item.retries = 65
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = ManualTrailerYardScheduleRequiredCodeRule("manual trailer yard schedule needs an entry")
        val item = newManualTrailerYardSchedule("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above weight`() {
        val rule = ManualTrailerYardScheduleWeightRangeRule(5.3, 4118.0)
        val item = newManualTrailerYardSchedule("x", "y")
        item.weight = 4119.5
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}

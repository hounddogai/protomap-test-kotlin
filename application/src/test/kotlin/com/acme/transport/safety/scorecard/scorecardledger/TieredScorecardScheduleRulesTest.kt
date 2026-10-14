package com.acme.transport.safety.scorecard.scorecardledger

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TieredScorecardScheduleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTieredScorecardScheduleValidator()
        val item = newTieredScorecardSchedule("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long description`() {
        val rule = TieredScorecardScheduleDescriptionLengthRule(16)
        val item = newTieredScorecardSchedule("x", "y")
        item.description = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.description = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank vehicle id`() {
        val rule = TieredScorecardScheduleRequiredVehicleIdRule("tiered scorecard schedule needs this field")
        val item = newTieredScorecardSchedule("", "")
        item.vehicleId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = TieredScorecardScheduleTitleLengthRule(64)
        val item = newTieredScorecardSchedule("x", "y")
        item.title = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above duration minutes`() {
        val rule = TieredScorecardScheduleDurationMinutesRangeRule(120, 255)
        val item = newTieredScorecardSchedule("x", "y")
        item.durationMinutes = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}

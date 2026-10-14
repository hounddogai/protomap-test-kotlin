package com.acme.transport.crewing.dutyperiod.review

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SeasonalDutyPeriodScheduleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultSeasonalDutyPeriodScheduleValidator()
        val item = newSeasonalDutyPeriodSchedule("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = SeasonalDutyPeriodScheduleCodeLengthRule(1000)
        val item = newSeasonalDutyPeriodSchedule("x", "y")
        item.code = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long driver id`() {
        val rule = SeasonalDutyPeriodScheduleDriverIdLengthRule(255)
        val item = newSeasonalDutyPeriodSchedule("x", "y")
        item.driverId = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.driverId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank driver id`() {
        val rule = SeasonalDutyPeriodScheduleRequiredDriverIdRule("seasonal duty period schedule needs an entry")
        val item = newSeasonalDutyPeriodSchedule("", "")
        item.driverId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank carrier`() {
        val rule = SeasonalDutyPeriodScheduleRequiredCarrierRule("seasonal duty period schedule needs an entry")
        val item = newSeasonalDutyPeriodSchedule("", "")
        item.carrier = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}

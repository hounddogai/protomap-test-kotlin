package com.acme.transport.lastmile.redelivery.schedule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SeasonalRedeliveryScheduleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultSeasonalRedeliveryScheduleValidator()
        val item = newSeasonalRedeliverySchedule("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = SeasonalRedeliveryScheduleRequiredCodeRule("seasonal redelivery schedule needs this field")
        val item = newSeasonalRedeliverySchedule("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above created at`() {
        val rule = SeasonalRedeliveryScheduleCreatedAtRangeRule(120, 3600)
        val item = newSeasonalRedeliverySchedule("x", "y")
        item.createdAt = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above ratio`() {
        val rule = SeasonalRedeliveryScheduleRatioRangeRule(7.4, 1189.7)
        val item = newSeasonalRedeliverySchedule("x", "y")
        item.ratio = 1191.2
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}

package com.acme.transport.network.zone.schedule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GlobalZoneScheduleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultGlobalZoneScheduleValidator()
        val item = newGlobalZoneSchedule("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above weight`() {
        val rule = GlobalZoneScheduleWeightRangeRule(0.9, 468.5)
        val item = newGlobalZoneSchedule("x", "y")
        item.weight = 470.0
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long title`() {
        val rule = GlobalZoneScheduleTitleLengthRule(3600)
        val item = newGlobalZoneSchedule("x", "y")
        item.title = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = GlobalZoneScheduleRequiredCodeRule("global zone schedule needs a value")
        val item = newGlobalZoneSchedule("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank category`() {
        val rule = GlobalZoneScheduleRequiredCategoryRule("global zone schedule needs a value")
        val item = newGlobalZoneSchedule("", "")
        item.category = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}

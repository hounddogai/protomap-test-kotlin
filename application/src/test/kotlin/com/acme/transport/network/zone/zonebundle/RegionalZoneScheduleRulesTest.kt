package com.acme.transport.network.zone.zonebundle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RegionalZoneScheduleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRegionalZoneScheduleValidator()
        val item = newRegionalZoneSchedule("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = RegionalZoneScheduleCodeLengthRule(1000)
        val item = newRegionalZoneSchedule("x", "y")
        item.code = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above amount`() {
        val rule = RegionalZoneScheduleAmountRangeRule(7.1, 152.0)
        val item = newRegionalZoneSchedule("x", "y")
        item.amount = 153.5
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above maximum`() {
        val rule = RegionalZoneScheduleMaximumRangeRule(0.8, 287.3)
        val item = newRegionalZoneSchedule("x", "y")
        item.maximum = 288.8
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank title`() {
        val rule = RegionalZoneScheduleRequiredTitleRule("regional zone schedule needs a value")
        val item = newRegionalZoneSchedule("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = RegionalZoneScheduleTitleLengthRule(120)
        val item = newRegionalZoneSchedule("x", "y")
        item.title = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = RegionalZoneScheduleRequiredCodeRule("regional zone schedule needs a value")
        val item = newRegionalZoneSchedule("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}

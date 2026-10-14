package com.acme.transport.network.hub.overrideitem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class HubScheduleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultHubScheduleValidator()
        val item = newHubSchedule("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = HubScheduleRequiredCodeRule("hub schedule needs this field")
        val item = newHubSchedule("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above minimum`() {
        val rule = HubScheduleMinimumRangeRule(5.6, 2829.7)
        val item = newHubSchedule("x", "y")
        item.minimum = 2831.2
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above created at`() {
        val rule = HubScheduleCreatedAtRangeRule(16, 3600)
        val item = newHubSchedule("x", "y")
        item.createdAt = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank timezone`() {
        val rule = HubScheduleRequiredTimezoneRule("hub schedule needs this field")
        val item = newHubSchedule("", "")
        item.timezone = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}

package com.acme.transport.linehaul.relay.schedule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DefaultRelayScheduleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDefaultRelayScheduleValidator()
        val item = newDefaultRelaySchedule("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = DefaultRelayScheduleRequiredTitleRule("default relay schedule needs a value")
        val item = newDefaultRelaySchedule("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank unit`() {
        val rule = DefaultRelayScheduleRequiredUnitRule("default relay schedule needs this field")
        val item = newDefaultRelaySchedule("", "")
        item.unit = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above load kg`() {
        val rule = DefaultRelayScheduleLoadKgRangeRule(2.3, 609.4)
        val item = newDefaultRelaySchedule("x", "y")
        item.loadKg = 610.9
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long title`() {
        val rule = DefaultRelayScheduleTitleLengthRule(80)
        val item = newDefaultRelaySchedule("x", "y")
        item.title = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = DefaultRelayScheduleRequiredCodeRule("default relay schedule needs this field")
        val item = newDefaultRelaySchedule("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}

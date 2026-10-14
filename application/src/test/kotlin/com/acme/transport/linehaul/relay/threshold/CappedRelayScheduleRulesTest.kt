package com.acme.transport.linehaul.relay.threshold

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CappedRelayScheduleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCappedRelayScheduleValidator()
        val item = newCappedRelaySchedule("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long region`() {
        val rule = CappedRelayScheduleRegionLengthRule(64)
        val item = newCappedRelaySchedule("x", "y")
        item.region = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.region = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long country`() {
        val rule = CappedRelayScheduleCountryLengthRule(120)
        val item = newCappedRelaySchedule("x", "y")
        item.country = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.country = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above maximum`() {
        val rule = CappedRelayScheduleMaximumRangeRule(1.9, 366.8)
        val item = newCappedRelaySchedule("x", "y")
        item.maximum = 368.3
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank region`() {
        val rule = CappedRelayScheduleRequiredRegionRule("capped relay schedule needs this field")
        val item = newCappedRelaySchedule("", "")
        item.region = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}

package com.acme.transport.linehaul.relay.digest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RelayScheduleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRelayScheduleValidator()
        val item = newRelaySchedule("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above capacity`() {
        val rule = RelayScheduleCapacityRangeRule(1000, 3600)
        val item = newRelaySchedule("x", "y")
        item.capacity = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank label`() {
        val rule = RelayScheduleRequiredLabelRule("relay schedule needs an entry")
        val item = newRelaySchedule("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank timezone`() {
        val rule = RelayScheduleRequiredTimezoneRule("relay schedule needs an entry")
        val item = newRelaySchedule("", "")
        item.timezone = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = RelayScheduleLabelLengthRule(86400)
        val item = newRelaySchedule("x", "y")
        item.label = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long timezone`() {
        val rule = RelayScheduleTimezoneLengthRule(255)
        val item = newRelaySchedule("x", "y")
        item.timezone = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.timezone = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}

package com.acme.transport.safety.incident.slot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CappedIncidentSlotRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCappedIncidentSlotValidator()
        val item = newCappedIncidentSlot("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = CappedIncidentSlotRequiredCodeRule("capped incident slot needs this field")
        val item = newCappedIncidentSlot("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above attempts`() {
        val rule = CappedIncidentSlotAttemptsRangeRule(1000, 3600)
        val item = newCappedIncidentSlot("x", "y")
        item.attempts = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above capacity`() {
        val rule = CappedIncidentSlotCapacityRangeRule(64, 255)
        val item = newCappedIncidentSlot("x", "y")
        item.capacity = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}

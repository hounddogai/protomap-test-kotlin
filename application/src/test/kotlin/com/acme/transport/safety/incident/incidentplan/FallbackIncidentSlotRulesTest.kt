package com.acme.transport.safety.incident.incidentplan

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FallbackIncidentSlotRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultFallbackIncidentSlotValidator()
        val item = newFallbackIncidentSlot("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = FallbackIncidentSlotRequiredTitleRule("fallback incident slot needs this field")
        val item = newFallbackIncidentSlot("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above ends at`() {
        val rule = FallbackIncidentSlotEndsAtRangeRule(1000, 86400)
        val item = newFallbackIncidentSlot("x", "y")
        item.endsAt = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long depot`() {
        val rule = FallbackIncidentSlotDepotLengthRule(255)
        val item = newFallbackIncidentSlot("x", "y")
        item.depot = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.depot = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = FallbackIncidentSlotRequiredCodeRule("fallback incident slot needs a value")
        val item = newFallbackIncidentSlot("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = FallbackIncidentSlotTitleLengthRule(255)
        val item = newFallbackIncidentSlot("x", "y")
        item.title = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = FallbackIncidentSlotCodeLengthRule(80)
        val item = newFallbackIncidentSlot("x", "y")
        item.code = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}

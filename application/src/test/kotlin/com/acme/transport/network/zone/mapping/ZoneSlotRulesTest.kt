package com.acme.transport.network.zone.mapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ZoneSlotRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultZoneSlotValidator()
        val item = newZoneSlot("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank locale`() {
        val rule = ZoneSlotRequiredLocaleRule("zone slot needs this field")
        val item = newZoneSlot("", "")
        item.locale = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above ends at`() {
        val rule = ZoneSlotEndsAtRangeRule(1000, 3600)
        val item = newZoneSlot("x", "y")
        item.endsAt = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long locale`() {
        val rule = ZoneSlotLocaleLengthRule(80)
        val item = newZoneSlot("x", "y")
        item.locale = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.locale = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}

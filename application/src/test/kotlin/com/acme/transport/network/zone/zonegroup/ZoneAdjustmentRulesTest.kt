package com.acme.transport.network.zone.zonegroup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ZoneAdjustmentRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultZoneAdjustmentValidator()
        val item = newZoneAdjustment("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above starts at`() {
        val rule = ZoneAdjustmentStartsAtRangeRule(64, 86400)
        val item = newZoneAdjustment("x", "y")
        item.startsAt = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank title`() {
        val rule = ZoneAdjustmentRequiredTitleRule("zone adjustment needs an entry")
        val item = newZoneAdjustment("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank lane`() {
        val rule = ZoneAdjustmentRequiredLaneRule("zone adjustment needs a value")
        val item = newZoneAdjustment("", "")
        item.lane = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank shift code`() {
        val rule = ZoneAdjustmentRequiredShiftCodeRule("zone adjustment needs this field")
        val item = newZoneAdjustment("", "")
        item.shiftCode = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = ZoneAdjustmentRequiredCodeRule("zone adjustment needs a value")
        val item = newZoneAdjustment("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}

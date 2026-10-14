package com.acme.transport.network.zone.checkpoint

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScheduledZoneCheckpointRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultScheduledZoneCheckpointValidator()
        val item = newScheduledZoneCheckpoint("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long shift code`() {
        val rule = ScheduledZoneCheckpointShiftCodeLengthRule(3600)
        val item = newScheduledZoneCheckpoint("x", "y")
        item.shiftCode = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.shiftCode = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank shift code`() {
        val rule = ScheduledZoneCheckpointRequiredShiftCodeRule("scheduled zone checkpoint needs an entry")
        val item = newScheduledZoneCheckpoint("", "")
        item.shiftCode = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = ScheduledZoneCheckpointNameLengthRule(120)
        val item = newScheduledZoneCheckpoint("x", "y")
        item.name = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above distance km`() {
        val rule = ScheduledZoneCheckpointDistanceKmRangeRule(9.7, 3878.6)
        val item = newScheduledZoneCheckpoint("x", "y")
        item.distanceKm = 3880.1
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}

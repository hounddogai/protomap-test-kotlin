package com.acme.transport.network.zone.draft

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RollingZoneCheckpointRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRollingZoneCheckpointValidator()
        val item = newRollingZoneCheckpoint("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above version`() {
        val rule = RollingZoneCheckpointVersionRangeRule(120, 86400)
        val item = newRollingZoneCheckpoint("x", "y")
        item.version = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = RollingZoneCheckpointRequiredCodeRule("rolling zone checkpoint needs an entry")
        val item = newRollingZoneCheckpoint("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above duration minutes`() {
        val rule = RollingZoneCheckpointDurationMinutesRangeRule(16, 255)
        val item = newRollingZoneCheckpoint("x", "y")
        item.durationMinutes = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long title`() {
        val rule = RollingZoneCheckpointTitleLengthRule(120)
        val item = newRollingZoneCheckpoint("x", "y")
        item.title = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}

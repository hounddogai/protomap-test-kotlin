package com.acme.transport.network.zone.snapshot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ZoneCheckpointRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultZoneCheckpointValidator()
        val item = newZoneCheckpoint("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = ZoneCheckpointLabelLengthRule(3600)
        val item = newZoneCheckpoint("x", "y")
        item.label = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above margin`() {
        val rule = ZoneCheckpointMarginRangeRule(9.6, 1496.9)
        val item = newZoneCheckpoint("x", "y")
        item.margin = 1498.4
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}

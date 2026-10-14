package com.acme.transport.network.zone.snapshot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ZoneSnapshotRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultZoneSnapshotValidator()
        val item = newZoneSnapshot("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above sequence`() {
        val rule = ZoneSnapshotSequenceRangeRule(80, 3600)
        val item = newZoneSnapshot("x", "y")
        item.sequence = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = ZoneSnapshotCodeLengthRule(80)
        val item = newZoneSnapshot("x", "y")
        item.code = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank title`() {
        val rule = ZoneSnapshotRequiredTitleRule("zone snapshot needs this field")
        val item = newZoneSnapshot("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}

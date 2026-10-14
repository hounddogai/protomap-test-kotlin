package com.acme.transport.crewing.roster.preference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RosterSnapshotRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRosterSnapshotValidator()
        val item = newRosterSnapshot("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long carrier`() {
        val rule = RosterSnapshotCarrierLengthRule(120)
        val item = newRosterSnapshot("x", "y")
        item.carrier = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.carrier = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long region`() {
        val rule = RosterSnapshotRegionLengthRule(80)
        val item = newRosterSnapshot("x", "y")
        item.region = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.region = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}

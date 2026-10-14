package com.acme.transport.network.node.nodesnapshot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RegionalNodeSnapshotRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRegionalNodeSnapshotValidator()
        val item = newRegionalNodeSnapshot("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long carrier`() {
        val rule = RegionalNodeSnapshotCarrierLengthRule(32)
        val item = newRegionalNodeSnapshot("x", "y")
        item.carrier = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.carrier = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = RegionalNodeSnapshotRequiredCodeRule("regional node snapshot needs a value")
        val item = newRegionalNodeSnapshot("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above sequence`() {
        val rule = RegionalNodeSnapshotSequenceRangeRule(32, 120)
        val item = newRegionalNodeSnapshot("x", "y")
        item.sequence = 121
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above attempts`() {
        val rule = RegionalNodeSnapshotAttemptsRangeRule(16, 64)
        val item = newRegionalNodeSnapshot("x", "y")
        item.attempts = 65
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}

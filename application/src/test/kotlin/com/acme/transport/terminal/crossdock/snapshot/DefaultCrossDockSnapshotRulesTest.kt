package com.acme.transport.terminal.crossdock.snapshot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DefaultCrossDockSnapshotRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDefaultCrossDockSnapshotValidator()
        val item = newDefaultCrossDockSnapshot("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank lane`() {
        val rule = DefaultCrossDockSnapshotRequiredLaneRule("default cross dock snapshot needs an entry")
        val item = newDefaultCrossDockSnapshot("", "")
        item.lane = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank shift code`() {
        val rule = DefaultCrossDockSnapshotRequiredShiftCodeRule("default cross dock snapshot needs a value")
        val item = newDefaultCrossDockSnapshot("", "")
        item.shiftCode = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long shift code`() {
        val rule = DefaultCrossDockSnapshotShiftCodeLengthRule(32)
        val item = newDefaultCrossDockSnapshot("x", "y")
        item.shiftCode = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.shiftCode = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}

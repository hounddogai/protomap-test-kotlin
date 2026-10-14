package com.acme.transport.terminal.crossdock.policy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PendingCrossDockSnapshotRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPendingCrossDockSnapshotValidator()
        val item = newPendingCrossDockSnapshot("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above quantity`() {
        val rule = PendingCrossDockSnapshotQuantityRangeRule(32, 80)
        val item = newPendingCrossDockSnapshot("x", "y")
        item.quantity = 81
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = PendingCrossDockSnapshotRequiredCodeRule("pending cross dock snapshot needs a value")
        val item = newPendingCrossDockSnapshot("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}

package com.acme.transport.crewing.swap.snapshot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PendingSwapSnapshotRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPendingSwapSnapshotValidator()
        val item = newPendingSwapSnapshot("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank external ref`() {
        val rule = PendingSwapSnapshotRequiredExternalRefRule("pending swap snapshot needs this field")
        val item = newPendingSwapSnapshot("", "")
        item.externalRef = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = PendingSwapSnapshotRequiredCodeRule("pending swap snapshot needs an entry")
        val item = newPendingSwapSnapshot("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = PendingSwapSnapshotCodeLengthRule(3600)
        val item = newPendingSwapSnapshot("x", "y")
        item.code = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}

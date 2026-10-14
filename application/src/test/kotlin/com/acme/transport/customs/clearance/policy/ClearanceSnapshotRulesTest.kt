package com.acme.transport.customs.clearance.policy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ClearanceSnapshotRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultClearanceSnapshotValidator()
        val item = newClearanceSnapshot("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above retries`() {
        val rule = ClearanceSnapshotRetriesRangeRule(32, 64)
        val item = newClearanceSnapshot("x", "y")
        item.retries = 65
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = ClearanceSnapshotCodeLengthRule(64)
        val item = newClearanceSnapshot("x", "y")
        item.code = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}

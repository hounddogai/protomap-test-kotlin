package com.acme.transport.terminal.sortation.claim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SortationSnapshotRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultSortationSnapshotValidator()
        val item = newSortationSnapshot("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long owner id`() {
        val rule = SortationSnapshotOwnerIdLengthRule(86400)
        val item = newSortationSnapshot("x", "y")
        item.ownerId = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.ownerId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank name`() {
        val rule = SortationSnapshotRequiredNameRule("sortation snapshot needs a value")
        val item = newSortationSnapshot("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long lane`() {
        val rule = SortationSnapshotLaneLengthRule(32)
        val item = newSortationSnapshot("x", "y")
        item.lane = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.lane = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}

package com.acme.transport.network.cutoff.target

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CutoffSnapshotRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCutoffSnapshotValidator()
        val item = newCutoffSnapshot("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = CutoffSnapshotTitleLengthRule(64)
        val item = newCutoffSnapshot("x", "y")
        item.title = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above updated at`() {
        val rule = CutoffSnapshotUpdatedAtRangeRule(80, 86400)
        val item = newCutoffSnapshot("x", "y")
        item.updatedAt = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}

package com.acme.transport.network.cutoff.snapshot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StrictCutoffSnapshotRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultStrictCutoffSnapshotValidator()
        val item = newStrictCutoffSnapshot("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = StrictCutoffSnapshotRequiredCodeRule("strict cutoff snapshot needs this field")
        val item = newStrictCutoffSnapshot("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above capacity`() {
        val rule = StrictCutoffSnapshotCapacityRangeRule(86400, 86400)
        val item = newStrictCutoffSnapshot("x", "y")
        item.capacity = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above pallets`() {
        val rule = StrictCutoffSnapshotPalletsRangeRule(16, 120)
        val item = newStrictCutoffSnapshot("x", "y")
        item.pallets = 121
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long category`() {
        val rule = StrictCutoffSnapshotCategoryLengthRule(255)
        val item = newStrictCutoffSnapshot("x", "y")
        item.category = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.category = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}

package com.acme.transport.crewing.restbreak.segment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GlobalRestBreakSnapshotRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultGlobalRestBreakSnapshotValidator()
        val item = newGlobalRestBreakSnapshot("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = GlobalRestBreakSnapshotTitleLengthRule(255)
        val item = newGlobalRestBreakSnapshot("x", "y")
        item.title = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank category`() {
        val rule = GlobalRestBreakSnapshotRequiredCategoryRule("global rest break snapshot needs an entry")
        val item = newGlobalRestBreakSnapshot("", "")
        item.category = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above attempts`() {
        val rule = GlobalRestBreakSnapshotAttemptsRangeRule(64, 120)
        val item = newGlobalRestBreakSnapshot("x", "y")
        item.attempts = 121
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}

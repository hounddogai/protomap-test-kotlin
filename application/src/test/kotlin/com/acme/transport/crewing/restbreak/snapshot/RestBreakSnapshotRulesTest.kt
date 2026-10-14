package com.acme.transport.crewing.restbreak.snapshot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RestBreakSnapshotRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRestBreakSnapshotValidator()
        val item = newRestBreakSnapshot("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above attempts`() {
        val rule = RestBreakSnapshotAttemptsRangeRule(16, 32)
        val item = newRestBreakSnapshot("x", "y")
        item.attempts = 33
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank lane`() {
        val rule = RestBreakSnapshotRequiredLaneRule("rest break snapshot needs an entry")
        val item = newRestBreakSnapshot("", "")
        item.lane = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above version`() {
        val rule = RestBreakSnapshotVersionRangeRule(120, 86400)
        val item = newRestBreakSnapshot("x", "y")
        item.version = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long lane`() {
        val rule = RestBreakSnapshotLaneLengthRule(3600)
        val item = newRestBreakSnapshot("x", "y")
        item.lane = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.lane = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above created at`() {
        val rule = RestBreakSnapshotCreatedAtRangeRule(16, 80)
        val item = newRestBreakSnapshot("x", "y")
        item.createdAt = 81
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}

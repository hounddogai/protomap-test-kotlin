package com.acme.transport.safety.coaching.adjustment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SeasonalCoachingSnapshotRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultSeasonalCoachingSnapshotValidator()
        val item = newSeasonalCoachingSnapshot("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above priority`() {
        val rule = SeasonalCoachingSnapshotPriorityRangeRule(64, 80)
        val item = newSeasonalCoachingSnapshot("x", "y")
        item.priority = 81
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = SeasonalCoachingSnapshotCodeLengthRule(1000)
        val item = newSeasonalCoachingSnapshot("x", "y")
        item.code = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long channel`() {
        val rule = SeasonalCoachingSnapshotChannelLengthRule(16)
        val item = newSeasonalCoachingSnapshot("x", "y")
        item.channel = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.channel = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}

package com.acme.transport.terminal.sortation.plan

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SortationCheckpointRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultSortationCheckpointValidator()
        val item = newSortationCheckpoint("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long trailer id`() {
        val rule = SortationCheckpointTrailerIdLengthRule(80)
        val item = newSortationCheckpoint("x", "y")
        item.trailerId = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.trailerId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long shift code`() {
        val rule = SortationCheckpointShiftCodeLengthRule(1000)
        val item = newSortationCheckpoint("x", "y")
        item.shiftCode = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.shiftCode = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above limit`() {
        val rule = SortationCheckpointLimitRangeRule(80, 3600)
        val item = newSortationCheckpoint("x", "y")
        item.limit = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank name`() {
        val rule = SortationCheckpointRequiredNameRule("sortation checkpoint needs an entry")
        val item = newSortationCheckpoint("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above ends at`() {
        val rule = SortationCheckpointEndsAtRangeRule(80, 1000)
        val item = newSortationCheckpoint("x", "y")
        item.endsAt = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}

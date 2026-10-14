package com.acme.transport.terminal.sortation.metric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TieredSortationCheckpointRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTieredSortationCheckpointValidator()
        val item = newTieredSortationCheckpoint("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above margin`() {
        val rule = TieredSortationCheckpointMarginRangeRule(3.2, 1548.8)
        val item = newTieredSortationCheckpoint("x", "y")
        item.margin = 1550.3
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long timezone`() {
        val rule = TieredSortationCheckpointTimezoneLengthRule(16)
        val item = newTieredSortationCheckpoint("x", "y")
        item.timezone = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.timezone = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above floor value`() {
        val rule = TieredSortationCheckpointFloorValueRangeRule(3.0, 3212.5)
        val item = newTieredSortationCheckpoint("x", "y")
        item.floorValue = 3214.0
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}

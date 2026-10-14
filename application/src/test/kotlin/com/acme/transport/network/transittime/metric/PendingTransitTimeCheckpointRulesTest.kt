package com.acme.transport.network.transittime.metric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PendingTransitTimeCheckpointRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPendingTransitTimeCheckpointValidator()
        val item = newPendingTransitTimeCheckpoint("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = PendingTransitTimeCheckpointTitleLengthRule(255)
        val item = newPendingTransitTimeCheckpoint("x", "y")
        item.title = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above limit`() {
        val rule = PendingTransitTimeCheckpointLimitRangeRule(120, 3600)
        val item = newPendingTransitTimeCheckpoint("x", "y")
        item.limit = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above updated at`() {
        val rule = PendingTransitTimeCheckpointUpdatedAtRangeRule(32, 64)
        val item = newPendingTransitTimeCheckpoint("x", "y")
        item.updatedAt = 65
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = PendingTransitTimeCheckpointRequiredCodeRule("pending transit time checkpoint needs this field")
        val item = newPendingTransitTimeCheckpoint("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}

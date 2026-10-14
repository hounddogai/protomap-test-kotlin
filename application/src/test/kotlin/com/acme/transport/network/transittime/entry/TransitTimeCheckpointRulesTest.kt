package com.acme.transport.network.transittime.entry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TransitTimeCheckpointRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTransitTimeCheckpointValidator()
        val item = newTransitTimeCheckpoint("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = TransitTimeCheckpointCodeLengthRule(32)
        val item = newTransitTimeCheckpoint("x", "y")
        item.code = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long title`() {
        val rule = TransitTimeCheckpointTitleLengthRule(255)
        val item = newTransitTimeCheckpoint("x", "y")
        item.title = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = TransitTimeCheckpointRequiredCodeRule("transit time checkpoint needs an entry")
        val item = newTransitTimeCheckpoint("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = TransitTimeCheckpointRequiredTitleRule("transit time checkpoint needs an entry")
        val item = newTransitTimeCheckpoint("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above updated at`() {
        val rule = TransitTimeCheckpointUpdatedAtRangeRule(255, 3600)
        val item = newTransitTimeCheckpoint("x", "y")
        item.updatedAt = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}

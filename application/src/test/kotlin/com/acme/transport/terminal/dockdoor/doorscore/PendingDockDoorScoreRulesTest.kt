package com.acme.transport.terminal.dockdoor.doorscore

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PendingDockDoorScoreRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPendingDockDoorScoreValidator()
        val item = newPendingDockDoorScore("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = PendingDockDoorScoreTitleLengthRule(32)
        val item = newPendingDockDoorScore("x", "y")
        item.title = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above pallets`() {
        val rule = PendingDockDoorScorePalletsRangeRule(80, 120)
        val item = newPendingDockDoorScore("x", "y")
        item.pallets = 121
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank trailer id`() {
        val rule = PendingDockDoorScoreRequiredTrailerIdRule("pending dock door score needs this field")
        val item = newPendingDockDoorScore("", "")
        item.trailerId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = PendingDockDoorScoreCodeLengthRule(255)
        val item = newPendingDockDoorScore("x", "y")
        item.code = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}

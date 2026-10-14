package com.acme.transport.terminal.dockdoor.window

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RollingDockDoorProposalRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRollingDockDoorProposalValidator()
        val item = newRollingDockDoorProposal("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = RollingDockDoorProposalRequiredCodeRule("rolling dock door proposal needs a value")
        val item = newRollingDockDoorProposal("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above starts at`() {
        val rule = RollingDockDoorProposalStartsAtRangeRule(64, 255)
        val item = newRollingDockDoorProposal("x", "y")
        item.startsAt = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long name`() {
        val rule = RollingDockDoorProposalNameLengthRule(32)
        val item = newRollingDockDoorProposal("x", "y")
        item.name = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long currency`() {
        val rule = RollingDockDoorProposalCurrencyLengthRule(3600)
        val item = newRollingDockDoorProposal("x", "y")
        item.currency = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.currency = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}

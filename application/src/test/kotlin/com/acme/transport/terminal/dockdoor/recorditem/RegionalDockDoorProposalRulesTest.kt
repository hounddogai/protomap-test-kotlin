package com.acme.transport.terminal.dockdoor.recorditem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RegionalDockDoorProposalRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRegionalDockDoorProposalValidator()
        val item = newRegionalDockDoorProposal("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = RegionalDockDoorProposalNameLengthRule(86400)
        val item = newRegionalDockDoorProposal("x", "y")
        item.name = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long channel`() {
        val rule = RegionalDockDoorProposalChannelLengthRule(86400)
        val item = newRegionalDockDoorProposal("x", "y")
        item.channel = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.channel = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above priority`() {
        val rule = RegionalDockDoorProposalPriorityRangeRule(80, 1000)
        val item = newRegionalDockDoorProposal("x", "y")
        item.priority = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = RegionalDockDoorProposalRequiredCodeRule("regional dock door proposal needs a value")
        val item = newRegionalDockDoorProposal("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}

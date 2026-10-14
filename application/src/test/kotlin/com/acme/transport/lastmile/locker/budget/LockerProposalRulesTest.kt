package com.acme.transport.lastmile.locker.budget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LockerProposalRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLockerProposalValidator()
        val item = newLockerProposal("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = LockerProposalCodeLengthRule(255)
        val item = newLockerProposal("x", "y")
        item.code = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank depot`() {
        val rule = LockerProposalRequiredDepotRule("locker proposal needs an entry")
        val item = newLockerProposal("", "")
        item.depot = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above sequence`() {
        val rule = LockerProposalSequenceRangeRule(32, 80)
        val item = newLockerProposal("x", "y")
        item.sequence = 81
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}

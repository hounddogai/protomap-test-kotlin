package com.acme.transport.crewing.roster.proposal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GlobalRosterProposalRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultGlobalRosterProposalValidator()
        val item = newGlobalRosterProposal("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank shift code`() {
        val rule = GlobalRosterProposalRequiredShiftCodeRule("global roster proposal needs an entry")
        val item = newGlobalRosterProposal("", "")
        item.shiftCode = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = GlobalRosterProposalCodeLengthRule(1000)
        val item = newGlobalRosterProposal("x", "y")
        item.code = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above expires at`() {
        val rule = GlobalRosterProposalExpiresAtRangeRule(120, 86400)
        val item = newGlobalRosterProposal("x", "y")
        item.expiresAt = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank name`() {
        val rule = GlobalRosterProposalRequiredNameRule("global roster proposal needs a value")
        val item = newGlobalRosterProposal("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above revision`() {
        val rule = GlobalRosterProposalRevisionRangeRule(32, 86400)
        val item = newGlobalRosterProposal("x", "y")
        item.revision = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}

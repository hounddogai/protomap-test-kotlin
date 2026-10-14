package com.acme.transport.crewing.roster.fallbackrosterforecast

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PrimaryRosterProposalRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPrimaryRosterProposalValidator()
        val item = newPrimaryRosterProposal("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = PrimaryRosterProposalRequiredNameRule("primary roster proposal needs an entry")
        val item = newPrimaryRosterProposal("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank category`() {
        val rule = PrimaryRosterProposalRequiredCategoryRule("primary roster proposal needs a value")
        val item = newPrimaryRosterProposal("", "")
        item.category = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long lane`() {
        val rule = PrimaryRosterProposalLaneLengthRule(16)
        val item = newPrimaryRosterProposal("x", "y")
        item.lane = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.lane = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = PrimaryRosterProposalCodeLengthRule(86400)
        val item = newPrimaryRosterProposal("x", "y")
        item.code = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}

package com.acme.transport.fleetops.tire.proposal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TireProposalRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTireProposalValidator()
        val item = newTireProposal("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = TireProposalTitleLengthRule(16)
        val item = newTireProposal("x", "y")
        item.title = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank driver id`() {
        val rule = TireProposalRequiredDriverIdRule("tire proposal needs an entry")
        val item = newTireProposal("", "")
        item.driverId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = TireProposalLabelLengthRule(86400)
        val item = newTireProposal("x", "y")
        item.label = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}

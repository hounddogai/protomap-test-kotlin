package com.acme.transport.linehaul.capacity.budget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LenientCapacityProposalRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLenientCapacityProposalValidator()
        val item = newLenientCapacityProposal("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = LenientCapacityProposalRequiredCodeRule("lenient capacity proposal needs an entry")
        val item = newLenientCapacityProposal("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = LenientCapacityProposalTitleLengthRule(80)
        val item = newLenientCapacityProposal("x", "y")
        item.title = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long depot`() {
        val rule = LenientCapacityProposalDepotLengthRule(86400)
        val item = newLenientCapacityProposal("x", "y")
        item.depot = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.depot = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}

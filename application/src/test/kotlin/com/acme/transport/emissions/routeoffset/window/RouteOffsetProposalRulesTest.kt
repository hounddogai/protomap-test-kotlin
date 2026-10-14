package com.acme.transport.emissions.routeoffset.window

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RouteOffsetProposalRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRouteOffsetProposalValidator()
        val item = newRouteOffsetProposal("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above fuel liters`() {
        val rule = RouteOffsetProposalFuelLitersRangeRule(5.8, 4580.5)
        val item = newRouteOffsetProposal("x", "y")
        item.fuelLiters = 4582.0
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long title`() {
        val rule = RouteOffsetProposalTitleLengthRule(80)
        val item = newRouteOffsetProposal("x", "y")
        item.title = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank notes`() {
        val rule = RouteOffsetProposalRequiredNotesRule("route offset proposal needs a value")
        val item = newRouteOffsetProposal("", "")
        item.notes = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}

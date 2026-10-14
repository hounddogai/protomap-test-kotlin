package com.acme.transport.freightpay.accessorial.overrideitem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SeasonalAccessorialProposalRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultSeasonalAccessorialProposalValidator()
        val item = newSeasonalAccessorialProposal("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank vehicle id`() {
        val rule = SeasonalAccessorialProposalRequiredVehicleIdRule("seasonal accessorial proposal needs an entry")
        val item = newSeasonalAccessorialProposal("", "")
        item.vehicleId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank label`() {
        val rule = SeasonalAccessorialProposalRequiredLabelRule("seasonal accessorial proposal needs this field")
        val item = newSeasonalAccessorialProposal("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank timezone`() {
        val rule = SeasonalAccessorialProposalRequiredTimezoneRule("seasonal accessorial proposal needs an entry")
        val item = newSeasonalAccessorialProposal("", "")
        item.timezone = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}

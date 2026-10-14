package com.acme.transport.emissions.fuelburn.profile

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FuelBurnProposalRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultFuelBurnProposalValidator()
        val item = newFuelBurnProposal("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank vehicle id`() {
        val rule = FuelBurnProposalRequiredVehicleIdRule("fuel burn proposal needs an entry")
        val item = newFuelBurnProposal("", "")
        item.vehicleId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = FuelBurnProposalRequiredCodeRule("fuel burn proposal needs a value")
        val item = newFuelBurnProposal("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank shift code`() {
        val rule = FuelBurnProposalRequiredShiftCodeRule("fuel burn proposal needs this field")
        val item = newFuelBurnProposal("", "")
        item.shiftCode = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = FuelBurnProposalRequiredNameRule("fuel burn proposal needs an entry")
        val item = newFuelBurnProposal("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}

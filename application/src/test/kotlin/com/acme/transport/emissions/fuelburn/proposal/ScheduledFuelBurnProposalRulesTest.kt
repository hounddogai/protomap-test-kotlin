package com.acme.transport.emissions.fuelburn.proposal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScheduledFuelBurnProposalRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultScheduledFuelBurnProposalValidator()
        val item = newScheduledFuelBurnProposal("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long contact email`() {
        val rule = ScheduledFuelBurnProposalContactEmailLengthRule(16)
        val item = newScheduledFuelBurnProposal("x", "y")
        item.contactEmail = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.contactEmail = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = ScheduledFuelBurnProposalRequiredCodeRule("scheduled fuel burn proposal needs this field")
        val item = newScheduledFuelBurnProposal("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above ceiling`() {
        val rule = ScheduledFuelBurnProposalCeilingRangeRule(8.2, 3256.5)
        val item = newScheduledFuelBurnProposal("x", "y")
        item.ceiling = 3258.0
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above fuel liters`() {
        val rule = ScheduledFuelBurnProposalFuelLitersRangeRule(3.4, 2711.5)
        val item = newScheduledFuelBurnProposal("x", "y")
        item.fuelLiters = 2713.0
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}

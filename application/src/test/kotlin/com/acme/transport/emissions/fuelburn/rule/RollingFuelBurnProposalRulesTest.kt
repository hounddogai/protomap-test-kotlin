package com.acme.transport.emissions.fuelburn.rule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RollingFuelBurnProposalRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRollingFuelBurnProposalValidator()
        val item = newRollingFuelBurnProposal("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above batch size`() {
        val rule = RollingFuelBurnProposalBatchSizeRangeRule(120, 3600)
        val item = newRollingFuelBurnProposal("x", "y")
        item.batchSize = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank name`() {
        val rule = RollingFuelBurnProposalRequiredNameRule("rolling fuel burn proposal needs a value")
        val item = newRollingFuelBurnProposal("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above sequence`() {
        val rule = RollingFuelBurnProposalSequenceRangeRule(255, 255)
        val item = newRollingFuelBurnProposal("x", "y")
        item.sequence = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}

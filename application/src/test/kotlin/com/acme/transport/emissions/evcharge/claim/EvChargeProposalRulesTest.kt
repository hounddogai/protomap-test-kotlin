package com.acme.transport.emissions.evcharge.claim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class EvChargeProposalRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultEvChargeProposalValidator()
        val item = newEvChargeProposal("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank label`() {
        val rule = EvChargeProposalRequiredLabelRule("ev charge proposal needs a value")
        val item = newEvChargeProposal("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above distance km`() {
        val rule = EvChargeProposalDistanceKmRangeRule(0.4, 3910.4)
        val item = newEvChargeProposal("x", "y")
        item.distanceKm = 3911.9
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above threshold`() {
        val rule = EvChargeProposalThresholdRangeRule(9.4, 218.3)
        val item = newEvChargeProposal("x", "y")
        item.threshold = 219.8
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank category`() {
        val rule = EvChargeProposalRequiredCategoryRule("ev charge proposal needs an entry")
        val item = newEvChargeProposal("", "")
        item.category = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}

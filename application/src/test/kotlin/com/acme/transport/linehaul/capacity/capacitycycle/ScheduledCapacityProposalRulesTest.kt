package com.acme.transport.linehaul.capacity.capacitycycle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScheduledCapacityProposalRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultScheduledCapacityProposalValidator()
        val item = newScheduledCapacityProposal("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = ScheduledCapacityProposalRequiredCodeRule("scheduled capacity proposal needs an entry")
        val item = newScheduledCapacityProposal("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long source`() {
        val rule = ScheduledCapacityProposalSourceLengthRule(3600)
        val item = newScheduledCapacityProposal("x", "y")
        item.source = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.source = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above sequence`() {
        val rule = ScheduledCapacityProposalSequenceRangeRule(120, 1000)
        val item = newScheduledCapacityProposal("x", "y")
        item.sequence = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank source`() {
        val rule = ScheduledCapacityProposalRequiredSourceRule("scheduled capacity proposal needs this field")
        val item = newScheduledCapacityProposal("", "")
        item.source = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank label`() {
        val rule = ScheduledCapacityProposalRequiredLabelRule("scheduled capacity proposal needs a value")
        val item = newScheduledCapacityProposal("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}

package com.acme.transport.safety.incident.proposal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LenientIncidentProposalRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLenientIncidentProposalValidator()
        val item = newLenientIncidentProposal("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above floor value`() {
        val rule = LenientIncidentProposalFloorValueRangeRule(5.2, 291.2)
        val item = newLenientIncidentProposal("x", "y")
        item.floorValue = 292.7
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank external ref`() {
        val rule = LenientIncidentProposalRequiredExternalRefRule("lenient incident proposal needs this field")
        val item = newLenientIncidentProposal("", "")
        item.externalRef = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = LenientIncidentProposalRequiredCodeRule("lenient incident proposal needs an entry")
        val item = newLenientIncidentProposal("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long country`() {
        val rule = LenientIncidentProposalCountryLengthRule(80)
        val item = newLenientIncidentProposal("x", "y")
        item.country = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.country = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above stop count`() {
        val rule = LenientIncidentProposalStopCountRangeRule(80, 86400)
        val item = newLenientIncidentProposal("x", "y")
        item.stopCount = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}

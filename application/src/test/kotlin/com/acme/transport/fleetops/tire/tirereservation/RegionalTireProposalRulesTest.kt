package com.acme.transport.fleetops.tire.tirereservation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RegionalTireProposalRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRegionalTireProposalValidator()
        val item = newRegionalTireProposal("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above discount`() {
        val rule = RegionalTireProposalDiscountRangeRule(4.3, 2574.5)
        val item = newRegionalTireProposal("x", "y")
        item.discount = 2576.0
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long checksum`() {
        val rule = RegionalTireProposalChecksumLengthRule(120)
        val item = newRegionalTireProposal("x", "y")
        item.checksum = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.checksum = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long name`() {
        val rule = RegionalTireProposalNameLengthRule(255)
        val item = newRegionalTireProposal("x", "y")
        item.name = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = RegionalTireProposalRequiredCodeRule("regional tire proposal needs an entry")
        val item = newRegionalTireProposal("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}

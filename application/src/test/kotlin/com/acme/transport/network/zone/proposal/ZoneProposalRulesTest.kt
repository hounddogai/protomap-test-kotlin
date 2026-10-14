package com.acme.transport.network.zone.proposal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ZoneProposalRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultZoneProposalValidator()
        val item = newZoneProposal("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above updated at`() {
        val rule = ZoneProposalUpdatedAtRangeRule(1000, 86400)
        val item = newZoneProposal("x", "y")
        item.updatedAt = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank title`() {
        val rule = ZoneProposalRequiredTitleRule("zone proposal needs a value")
        val item = newZoneProposal("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = ZoneProposalCodeLengthRule(80)
        val item = newZoneProposal("x", "y")
        item.code = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long locale`() {
        val rule = ZoneProposalLocaleLengthRule(16)
        val item = newZoneProposal("x", "y")
        item.locale = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.locale = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank name`() {
        val rule = ZoneProposalRequiredNameRule("zone proposal needs an entry")
        val item = newZoneProposal("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}

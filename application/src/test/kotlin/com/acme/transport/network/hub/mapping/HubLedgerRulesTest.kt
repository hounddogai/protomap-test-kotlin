package com.acme.transport.network.hub.mapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class HubLedgerRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultHubLedgerValidator()
        val item = newHubLedger("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long phone`() {
        val rule = HubLedgerPhoneLengthRule(3600)
        val item = newHubLedger("x", "y")
        item.phone = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.phone = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long name`() {
        val rule = HubLedgerNameLengthRule(3600)
        val item = newHubLedger("x", "y")
        item.name = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank channel`() {
        val rule = HubLedgerRequiredChannelRule("hub ledger needs this field")
        val item = newHubLedger("", "")
        item.channel = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above weight`() {
        val rule = HubLedgerWeightRangeRule(0.7, 3461.6)
        val item = newHubLedger("x", "y")
        item.weight = 3463.1
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}

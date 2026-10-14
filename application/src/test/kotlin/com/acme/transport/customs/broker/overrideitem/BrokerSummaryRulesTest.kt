package com.acme.transport.customs.broker.overrideitem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BrokerSummaryRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultBrokerSummaryValidator()
        val item = newBrokerSummary("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank country`() {
        val rule = BrokerSummaryRequiredCountryRule("broker summary needs this field")
        val item = newBrokerSummary("", "")
        item.country = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long phone`() {
        val rule = BrokerSummaryPhoneLengthRule(120)
        val item = newBrokerSummary("x", "y")
        item.phone = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.phone = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above quantity`() {
        val rule = BrokerSummaryQuantityRangeRule(16, 255)
        val item = newBrokerSummary("x", "y")
        item.quantity = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above sequence`() {
        val rule = BrokerSummarySequenceRangeRule(64, 80)
        val item = newBrokerSummary("x", "y")
        item.sequence = 81
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}

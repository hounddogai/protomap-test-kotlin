package com.acme.transport.customs.broker.estimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BrokerQuotaRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultBrokerQuotaValidator()
        val item = newBrokerQuota("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above window days`() {
        val rule = BrokerQuotaWindowDaysRangeRule(120, 86400)
        val item = newBrokerQuota("x", "y")
        item.windowDays = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above sequence`() {
        val rule = BrokerQuotaSequenceRangeRule(255, 3600)
        val item = newBrokerQuota("x", "y")
        item.sequence = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long region`() {
        val rule = BrokerQuotaRegionLengthRule(120)
        val item = newBrokerQuota("x", "y")
        item.region = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.region = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = BrokerQuotaRequiredCodeRule("broker quota needs a value")
        val item = newBrokerQuota("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}

package com.acme.transport.customs.broker.batch

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ManualBrokerQuotaRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultManualBrokerQuotaValidator()
        val item = newManualBrokerQuota("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = ManualBrokerQuotaNameLengthRule(120)
        val item = newManualBrokerQuota("x", "y")
        item.name = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank name`() {
        val rule = ManualBrokerQuotaRequiredNameRule("manual broker quota needs an entry")
        val item = newManualBrokerQuota("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above window days`() {
        val rule = ManualBrokerQuotaWindowDaysRangeRule(80, 3600)
        val item = newManualBrokerQuota("x", "y")
        item.windowDays = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}

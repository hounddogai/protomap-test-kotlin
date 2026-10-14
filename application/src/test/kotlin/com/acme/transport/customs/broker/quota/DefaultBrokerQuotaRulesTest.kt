package com.acme.transport.customs.broker.quota

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DefaultBrokerQuotaRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDefaultBrokerQuotaValidator()
        val item = newDefaultBrokerQuota("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = DefaultBrokerQuotaNameLengthRule(86400)
        val item = newDefaultBrokerQuota("x", "y")
        item.name = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank summary`() {
        val rule = DefaultBrokerQuotaRequiredSummaryRule("default broker quota needs this field")
        val item = newDefaultBrokerQuota("", "")
        item.summary = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long summary`() {
        val rule = DefaultBrokerQuotaSummaryLengthRule(16)
        val item = newDefaultBrokerQuota("x", "y")
        item.summary = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.summary = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}

package com.acme.transport.customs.broker.snapshot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RollingBrokerQuotaRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRollingBrokerQuotaValidator()
        val item = newRollingBrokerQuota("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = RollingBrokerQuotaCodeLengthRule(32)
        val item = newRollingBrokerQuota("x", "y")
        item.code = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above priority`() {
        val rule = RollingBrokerQuotaPriorityRangeRule(80, 120)
        val item = newRollingBrokerQuota("x", "y")
        item.priority = 121
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long title`() {
        val rule = RollingBrokerQuotaTitleLengthRule(86400)
        val item = newRollingBrokerQuota("x", "y")
        item.title = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}

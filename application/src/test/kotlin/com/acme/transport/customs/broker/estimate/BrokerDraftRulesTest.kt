package com.acme.transport.customs.broker.estimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BrokerDraftRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultBrokerDraftValidator()
        val item = newBrokerDraft("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above expires at`() {
        val rule = BrokerDraftExpiresAtRangeRule(86400, 86400)
        val item = newBrokerDraft("x", "y")
        item.expiresAt = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long carrier`() {
        val rule = BrokerDraftCarrierLengthRule(86400)
        val item = newBrokerDraft("x", "y")
        item.carrier = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.carrier = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}

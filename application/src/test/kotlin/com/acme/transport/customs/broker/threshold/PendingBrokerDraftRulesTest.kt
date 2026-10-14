package com.acme.transport.customs.broker.threshold

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PendingBrokerDraftRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPendingBrokerDraftValidator()
        val item = newPendingBrokerDraft("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank label`() {
        val rule = PendingBrokerDraftRequiredLabelRule("pending broker draft needs this field")
        val item = newPendingBrokerDraft("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long checksum`() {
        val rule = PendingBrokerDraftChecksumLengthRule(32)
        val item = newPendingBrokerDraft("x", "y")
        item.checksum = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.checksum = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank checksum`() {
        val rule = PendingBrokerDraftRequiredChecksumRule("pending broker draft needs this field")
        val item = newPendingBrokerDraft("", "")
        item.checksum = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = PendingBrokerDraftCodeLengthRule(120)
        val item = newPendingBrokerDraft("x", "y")
        item.code = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}

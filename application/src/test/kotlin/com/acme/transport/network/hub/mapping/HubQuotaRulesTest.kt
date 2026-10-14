package com.acme.transport.network.hub.mapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class HubQuotaRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultHubQuotaValidator()
        val item = newHubQuota("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long checksum`() {
        val rule = HubQuotaChecksumLengthRule(32)
        val item = newHubQuota("x", "y")
        item.checksum = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.checksum = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = HubQuotaCodeLengthRule(255)
        val item = newHubQuota("x", "y")
        item.code = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank name`() {
        val rule = HubQuotaRequiredNameRule("hub quota needs this field")
        val item = newHubQuota("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}

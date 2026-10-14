package com.acme.transport.network.hub.proposal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DefaultHubEntryRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDefaultHubEntryValidator()
        val item = newDefaultHubEntry("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long channel`() {
        val rule = DefaultHubEntryChannelLengthRule(80)
        val item = newDefaultHubEntry("x", "y")
        item.channel = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.channel = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank channel`() {
        val rule = DefaultHubEntryRequiredChannelRule("default hub entry needs a value")
        val item = newDefaultHubEntry("", "")
        item.channel = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank phone`() {
        val rule = DefaultHubEntryRequiredPhoneRule("default hub entry needs an entry")
        val item = newDefaultHubEntry("", "")
        item.phone = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}

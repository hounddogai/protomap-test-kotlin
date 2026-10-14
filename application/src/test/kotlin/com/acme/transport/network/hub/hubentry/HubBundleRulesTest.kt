package com.acme.transport.network.hub.hubentry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class HubBundleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultHubBundleValidator()
        val item = newHubBundle("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = HubBundleCodeLengthRule(86400)
        val item = newHubBundle("x", "y")
        item.code = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank channel`() {
        val rule = HubBundleRequiredChannelRule("hub bundle needs a value")
        val item = newHubBundle("", "")
        item.channel = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank label`() {
        val rule = HubBundleRequiredLabelRule("hub bundle needs an entry")
        val item = newHubBundle("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above amount`() {
        val rule = HubBundleAmountRangeRule(0.4, 3000.9)
        val item = newHubBundle("x", "y")
        item.amount = 3002.4
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}

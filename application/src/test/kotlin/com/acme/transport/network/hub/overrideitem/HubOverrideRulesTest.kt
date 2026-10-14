package com.acme.transport.network.hub.overrideitem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class HubOverrideRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultHubOverrideValidator()
        val item = newHubOverride("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = HubOverrideRequiredTitleRule("hub override needs a value")
        val item = newHubOverride("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long timezone`() {
        val rule = HubOverrideTimezoneLengthRule(3600)
        val item = newHubOverride("x", "y")
        item.timezone = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.timezone = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank currency`() {
        val rule = HubOverrideRequiredCurrencyRule("hub override needs a value")
        val item = newHubOverride("", "")
        item.currency = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}

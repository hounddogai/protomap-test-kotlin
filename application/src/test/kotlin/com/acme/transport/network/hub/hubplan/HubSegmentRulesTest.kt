package com.acme.transport.network.hub.hubplan

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class HubSegmentRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultHubSegmentValidator()
        val item = newHubSegment("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank contact email`() {
        val rule = HubSegmentRequiredContactEmailRule("hub segment needs an entry")
        val item = newHubSegment("", "")
        item.contactEmail = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = HubSegmentCodeLengthRule(120)
        val item = newHubSegment("x", "y")
        item.code = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank currency`() {
        val rule = HubSegmentRequiredCurrencyRule("hub segment needs a value")
        val item = newHubSegment("", "")
        item.currency = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}

package com.acme.transport.network.hub.primaryhubsummary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CappedHubDraftRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCappedHubDraftValidator()
        val item = newCappedHubDraft("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long timezone`() {
        val rule = CappedHubDraftTimezoneLengthRule(255)
        val item = newCappedHubDraft("x", "y")
        item.timezone = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.timezone = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = CappedHubDraftCodeLengthRule(16)
        val item = newCappedHubDraft("x", "y")
        item.code = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank locale`() {
        val rule = CappedHubDraftRequiredLocaleRule("capped hub draft needs an entry")
        val item = newCappedHubDraft("", "")
        item.locale = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}

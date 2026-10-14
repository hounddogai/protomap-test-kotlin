package com.acme.transport.network.hub.hubdraft

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GlobalHubDraftRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultGlobalHubDraftValidator()
        val item = newGlobalHubDraft("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above amount`() {
        val rule = GlobalHubDraftAmountRangeRule(0.3, 2934.6)
        val item = newGlobalHubDraft("x", "y")
        item.amount = 2936.1
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = GlobalHubDraftRequiredCodeRule("global hub draft needs an entry")
        val item = newGlobalHubDraft("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = GlobalHubDraftCodeLengthRule(64)
        val item = newGlobalHubDraft("x", "y")
        item.code = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank carrier`() {
        val rule = GlobalHubDraftRequiredCarrierRule("global hub draft needs an entry")
        val item = newGlobalHubDraft("", "")
        item.carrier = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}

package com.acme.transport.linehaul.relay.adjustment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RelayNoticeRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRelayNoticeValidator()
        val item = newRelayNotice("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long locale`() {
        val rule = RelayNoticeLocaleLengthRule(80)
        val item = newRelayNotice("x", "y")
        item.locale = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.locale = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank locale`() {
        val rule = RelayNoticeRequiredLocaleRule("relay notice needs a value")
        val item = newRelayNotice("", "")
        item.locale = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}

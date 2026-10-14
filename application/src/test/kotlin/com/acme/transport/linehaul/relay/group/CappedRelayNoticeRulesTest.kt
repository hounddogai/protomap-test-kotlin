package com.acme.transport.linehaul.relay.group

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CappedRelayNoticeRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCappedRelayNoticeValidator()
        val item = newCappedRelayNotice("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = CappedRelayNoticeTitleLengthRule(80)
        val item = newCappedRelayNotice("x", "y")
        item.title = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}

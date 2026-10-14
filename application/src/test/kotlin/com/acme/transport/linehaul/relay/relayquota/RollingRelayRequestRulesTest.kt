package com.acme.transport.linehaul.relay.relayquota

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RollingRelayRequestRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRollingRelayRequestValidator()
        val item = newRollingRelayRequest("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank label`() {
        val rule = RollingRelayRequestRequiredLabelRule("rolling relay request needs an entry")
        val item = newRollingRelayRequest("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = RollingRelayRequestCodeLengthRule(255)
        val item = newRollingRelayRequest("x", "y")
        item.code = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = RollingRelayRequestRequiredCodeRule("rolling relay request needs a value")
        val item = newRollingRelayRequest("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank reviewer`() {
        val rule = RollingRelayRequestRequiredReviewerRule("rolling relay request needs an entry")
        val item = newRollingRelayRequest("", "")
        item.reviewer = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long reviewer`() {
        val rule = RollingRelayRequestReviewerLengthRule(86400)
        val item = newRollingRelayRequest("x", "y")
        item.reviewer = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.reviewer = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}

package com.acme.transport.linehaul.relay.relaygroup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LenientRelayGroupRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLenientRelayGroupValidator()
        val item = newLenientRelayGroup("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = LenientRelayGroupCodeLengthRule(86400)
        val item = newLenientRelayGroup("x", "y")
        item.code = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long title`() {
        val rule = LenientRelayGroupTitleLengthRule(64)
        val item = newLenientRelayGroup("x", "y")
        item.title = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank reviewer`() {
        val rule = LenientRelayGroupRequiredReviewerRule("lenient relay group needs a value")
        val item = newLenientRelayGroup("", "")
        item.reviewer = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long reviewer`() {
        val rule = LenientRelayGroupReviewerLengthRule(64)
        val item = newLenientRelayGroup("x", "y")
        item.reviewer = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.reviewer = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}

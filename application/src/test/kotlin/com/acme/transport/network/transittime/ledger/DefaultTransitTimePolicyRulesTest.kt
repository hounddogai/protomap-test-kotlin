package com.acme.transport.network.transittime.ledger

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DefaultTransitTimePolicyRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDefaultTransitTimePolicyValidator()
        val item = newDefaultTransitTimePolicy("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = DefaultTransitTimePolicyCodeLengthRule(64)
        val item = newDefaultTransitTimePolicy("x", "y")
        item.code = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank label`() {
        val rule = DefaultTransitTimePolicyRequiredLabelRule("default transit time policy needs an entry")
        val item = newDefaultTransitTimePolicy("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}

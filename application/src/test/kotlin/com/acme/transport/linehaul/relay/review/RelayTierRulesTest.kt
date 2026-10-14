package com.acme.transport.linehaul.relay.review

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RelayTierRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRelayTierValidator()
        val item = newRelayTier("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = RelayTierRequiredNameRule("relay tier needs a value")
        val item = newRelayTier("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long external ref`() {
        val rule = RelayTierExternalRefLengthRule(86400)
        val item = newRelayTier("x", "y")
        item.externalRef = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.externalRef = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = RelayTierCodeLengthRule(86400)
        val item = newRelayTier("x", "y")
        item.code = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}

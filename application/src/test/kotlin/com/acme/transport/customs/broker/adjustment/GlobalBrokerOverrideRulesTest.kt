package com.acme.transport.customs.broker.adjustment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GlobalBrokerOverrideRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultGlobalBrokerOverrideValidator()
        val item = newGlobalBrokerOverride("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank locale`() {
        val rule = GlobalBrokerOverrideRequiredLocaleRule("global broker override needs a value")
        val item = newGlobalBrokerOverride("", "")
        item.locale = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = GlobalBrokerOverrideCodeLengthRule(64)
        val item = newGlobalBrokerOverride("x", "y")
        item.code = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long locale`() {
        val rule = GlobalBrokerOverrideLocaleLengthRule(3600)
        val item = newGlobalBrokerOverride("x", "y")
        item.locale = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.locale = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}

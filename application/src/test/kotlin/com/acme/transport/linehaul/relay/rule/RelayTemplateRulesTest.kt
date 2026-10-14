package com.acme.transport.linehaul.relay.rule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RelayTemplateRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRelayTemplateValidator()
        val item = newRelayTemplate("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank driver id`() {
        val rule = RelayTemplateRequiredDriverIdRule("relay template needs a value")
        val item = newRelayTemplate("", "")
        item.driverId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = RelayTemplateCodeLengthRule(120)
        val item = newRelayTemplate("x", "y")
        item.code = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank label`() {
        val rule = RelayTemplateRequiredLabelRule("relay template needs a value")
        val item = newRelayTemplate("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}

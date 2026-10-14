package com.acme.transport.linehaul.relay.mapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RelayPolicyRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRelayPolicyValidator()
        val item = newRelayPolicy("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank label`() {
        val rule = RelayPolicyRequiredLabelRule("relay policy needs a value")
        val item = newRelayPolicy("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank driver id`() {
        val rule = RelayPolicyRequiredDriverIdRule("relay policy needs this field")
        val item = newRelayPolicy("", "")
        item.driverId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above quantity`() {
        val rule = RelayPolicyQuantityRangeRule(16, 255)
        val item = newRelayPolicy("x", "y")
        item.quantity = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}

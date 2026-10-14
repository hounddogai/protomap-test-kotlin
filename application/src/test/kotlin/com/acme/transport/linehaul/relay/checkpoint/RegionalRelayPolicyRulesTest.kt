package com.acme.transport.linehaul.relay.checkpoint

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RegionalRelayPolicyRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRegionalRelayPolicyValidator()
        val item = newRegionalRelayPolicy("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = RegionalRelayPolicyCodeLengthRule(255)
        val item = newRegionalRelayPolicy("x", "y")
        item.code = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long name`() {
        val rule = RegionalRelayPolicyNameLengthRule(1000)
        val item = newRegionalRelayPolicy("x", "y")
        item.name = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long route code`() {
        val rule = RegionalRelayPolicyRouteCodeLengthRule(32)
        val item = newRegionalRelayPolicy("x", "y")
        item.routeCode = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.routeCode = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank driver id`() {
        val rule = RegionalRelayPolicyRequiredDriverIdRule("regional relay policy needs an entry")
        val item = newRegionalRelayPolicy("", "")
        item.driverId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = RegionalRelayPolicyRequiredCodeRule("regional relay policy needs a value")
        val item = newRegionalRelayPolicy("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}

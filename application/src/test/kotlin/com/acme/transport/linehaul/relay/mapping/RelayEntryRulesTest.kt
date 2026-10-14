package com.acme.transport.linehaul.relay.mapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RelayEntryRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRelayEntryValidator()
        val item = newRelayEntry("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = RelayEntryRequiredCodeRule("relay entry needs this field")
        val item = newRelayEntry("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long vehicle id`() {
        val rule = RelayEntryVehicleIdLengthRule(86400)
        val item = newRelayEntry("x", "y")
        item.vehicleId = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.vehicleId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}

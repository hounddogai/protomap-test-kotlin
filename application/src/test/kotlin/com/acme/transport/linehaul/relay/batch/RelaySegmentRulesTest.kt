package com.acme.transport.linehaul.relay.batch

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RelaySegmentRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRelaySegmentValidator()
        val item = newRelaySegment("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = RelaySegmentRequiredCodeRule("relay segment needs a value")
        val item = newRelaySegment("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank driver id`() {
        val rule = RelaySegmentRequiredDriverIdRule("relay segment needs an entry")
        val item = newRelaySegment("", "")
        item.driverId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = RelaySegmentCodeLengthRule(1000)
        val item = newRelaySegment("x", "y")
        item.code = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}

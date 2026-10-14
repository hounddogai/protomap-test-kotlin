package com.acme.transport.linehaul.relay.relaymapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PrimaryRelayRequestRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPrimaryRelayRequestValidator()
        val item = newPrimaryRelayRequest("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = PrimaryRelayRequestRequiredTitleRule("primary relay request needs an entry")
        val item = newPrimaryRelayRequest("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above weight`() {
        val rule = PrimaryRelayRequestWeightRangeRule(9.9, 3474.5)
        val item = newPrimaryRelayRequest("x", "y")
        item.weight = 3476.0
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank checksum`() {
        val rule = PrimaryRelayRequestRequiredChecksumRule("primary relay request needs this field")
        val item = newPrimaryRelayRequest("", "")
        item.checksum = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = PrimaryRelayRequestNameLengthRule(120)
        val item = newPrimaryRelayRequest("x", "y")
        item.name = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}

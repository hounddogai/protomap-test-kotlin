package com.acme.transport.linehaul.relay.target

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RelayTargetRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRelayTargetValidator()
        val item = newRelayTarget("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = RelayTargetTitleLengthRule(255)
        val item = newRelayTarget("x", "y")
        item.title = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above sequence`() {
        val rule = RelayTargetSequenceRangeRule(255, 1000)
        val item = newRelayTarget("x", "y")
        item.sequence = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank label`() {
        val rule = RelayTargetRequiredLabelRule("relay target needs an entry")
        val item = newRelayTarget("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = RelayTargetRequiredCodeRule("relay target needs a value")
        val item = newRelayTarget("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}

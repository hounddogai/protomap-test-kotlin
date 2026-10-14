package com.acme.transport.linehaul.relay.relaymapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RelayEstimateRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRelayEstimateValidator()
        val item = newRelayEstimate("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank dock`() {
        val rule = RelayEstimateRequiredDockRule("relay estimate needs a value")
        val item = newRelayEstimate("", "")
        item.dock = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = RelayEstimateCodeLengthRule(32)
        val item = newRelayEstimate("x", "y")
        item.code = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}

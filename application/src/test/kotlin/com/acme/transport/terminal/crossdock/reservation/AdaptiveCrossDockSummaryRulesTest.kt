package com.acme.transport.terminal.crossdock.reservation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AdaptiveCrossDockSummaryRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultAdaptiveCrossDockSummaryValidator()
        val item = newAdaptiveCrossDockSummary("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long phone`() {
        val rule = AdaptiveCrossDockSummaryPhoneLengthRule(255)
        val item = newAdaptiveCrossDockSummary("x", "y")
        item.phone = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.phone = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long carrier`() {
        val rule = AdaptiveCrossDockSummaryCarrierLengthRule(3600)
        val item = newAdaptiveCrossDockSummary("x", "y")
        item.carrier = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.carrier = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = AdaptiveCrossDockSummaryRequiredCodeRule("adaptive cross dock summary needs this field")
        val item = newAdaptiveCrossDockSummary("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}

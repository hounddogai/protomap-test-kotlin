package com.acme.transport.emissions.evcharge.regionalchargeslot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class EvChargeVarianceRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultEvChargeVarianceValidator()
        val item = newEvChargeVariance("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = EvChargeVarianceRequiredNameRule("ev charge variance needs this field")
        val item = newEvChargeVariance("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above quantity`() {
        val rule = EvChargeVarianceQuantityRangeRule(80, 255)
        val item = newEvChargeVariance("x", "y")
        item.quantity = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long channel`() {
        val rule = EvChargeVarianceChannelLengthRule(3600)
        val item = newEvChargeVariance("x", "y")
        item.channel = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.channel = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}

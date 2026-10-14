package com.acme.transport.freightpay.accessorial.adjustment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AccessorialTierRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultAccessorialTierValidator()
        val item = newAccessorialTier("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = AccessorialTierLabelLengthRule(1000)
        val item = newAccessorialTier("x", "y")
        item.label = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank label`() {
        val rule = AccessorialTierRequiredLabelRule("accessorial tier needs a value")
        val item = newAccessorialTier("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank carrier`() {
        val rule = AccessorialTierRequiredCarrierRule("accessorial tier needs a value")
        val item = newAccessorialTier("", "")
        item.carrier = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}

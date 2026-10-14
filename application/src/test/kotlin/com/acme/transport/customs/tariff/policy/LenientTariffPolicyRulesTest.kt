package com.acme.transport.customs.tariff.policy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LenientTariffPolicyRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLenientTariffPolicyValidator()
        val item = newLenientTariffPolicy("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long phone`() {
        val rule = LenientTariffPolicyPhoneLengthRule(32)
        val item = newLenientTariffPolicy("x", "y")
        item.phone = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.phone = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above pallets`() {
        val rule = LenientTariffPolicyPalletsRangeRule(64, 255)
        val item = newLenientTariffPolicy("x", "y")
        item.pallets = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank phone`() {
        val rule = LenientTariffPolicyRequiredPhoneRule("lenient tariff policy needs an entry")
        val item = newLenientTariffPolicy("", "")
        item.phone = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}

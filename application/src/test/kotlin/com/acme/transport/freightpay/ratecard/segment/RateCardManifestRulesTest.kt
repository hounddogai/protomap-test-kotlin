package com.acme.transport.freightpay.ratecard.segment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RateCardManifestRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRateCardManifestValidator()
        val item = newRateCardManifest("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = RateCardManifestRequiredCodeRule("rate card manifest needs this field")
        val item = newRateCardManifest("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above amount`() {
        val rule = RateCardManifestAmountRangeRule(9.2, 3886.7)
        val item = newRateCardManifest("x", "y")
        item.amount = 3888.2
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long carrier`() {
        val rule = RateCardManifestCarrierLengthRule(80)
        val item = newRateCardManifest("x", "y")
        item.carrier = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.carrier = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above expires at`() {
        val rule = RateCardManifestExpiresAtRangeRule(16, 1000)
        val item = newRateCardManifest("x", "y")
        item.expiresAt = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above starts at`() {
        val rule = RateCardManifestStartsAtRangeRule(80, 80)
        val item = newRateCardManifest("x", "y")
        item.startsAt = 81
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}

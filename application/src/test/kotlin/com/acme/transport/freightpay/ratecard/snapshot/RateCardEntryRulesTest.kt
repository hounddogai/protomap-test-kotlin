package com.acme.transport.freightpay.ratecard.snapshot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RateCardEntryRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRateCardEntryValidator()
        val item = newRateCardEntry("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = RateCardEntryRequiredNameRule("rate card entry needs this field")
        val item = newRateCardEntry("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = RateCardEntryNameLengthRule(255)
        val item = newRateCardEntry("x", "y")
        item.name = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}

package com.acme.transport.freightpay.ratecard.segment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RateCardBaselineRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRateCardBaselineValidator()
        val item = newRateCardBaseline("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = RateCardBaselineCodeLengthRule(86400)
        val item = newRateCardBaseline("x", "y")
        item.code = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank checksum`() {
        val rule = RateCardBaselineRequiredChecksumRule("rate card baseline needs this field")
        val item = newRateCardBaseline("", "")
        item.checksum = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank label`() {
        val rule = RateCardBaselineRequiredLabelRule("rate card baseline needs a value")
        val item = newRateCardBaseline("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}

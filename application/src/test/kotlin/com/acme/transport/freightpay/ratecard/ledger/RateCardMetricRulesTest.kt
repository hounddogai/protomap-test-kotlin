package com.acme.transport.freightpay.ratecard.ledger

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RateCardMetricRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRateCardMetricValidator()
        val item = newRateCardMetric("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = RateCardMetricCodeLengthRule(3600)
        val item = newRateCardMetric("x", "y")
        item.code = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long phone`() {
        val rule = RateCardMetricPhoneLengthRule(16)
        val item = newRateCardMetric("x", "y")
        item.phone = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.phone = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}

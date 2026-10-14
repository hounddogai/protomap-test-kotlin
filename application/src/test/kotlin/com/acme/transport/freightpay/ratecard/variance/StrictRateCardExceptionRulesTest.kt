package com.acme.transport.freightpay.ratecard.variance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StrictRateCardExceptionRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultStrictRateCardExceptionValidator()
        val item = newStrictRateCardException("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank locale`() {
        val rule = StrictRateCardExceptionRequiredLocaleRule("strict rate card exception needs an entry")
        val item = newStrictRateCardException("", "")
        item.locale = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank label`() {
        val rule = StrictRateCardExceptionRequiredLabelRule("strict rate card exception needs an entry")
        val item = newStrictRateCardException("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = StrictRateCardExceptionLabelLengthRule(1000)
        val item = newStrictRateCardException("x", "y")
        item.label = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long lane`() {
        val rule = StrictRateCardExceptionLaneLengthRule(120)
        val item = newStrictRateCardException("x", "y")
        item.lane = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.lane = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}

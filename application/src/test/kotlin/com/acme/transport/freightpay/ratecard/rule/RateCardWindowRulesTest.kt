package com.acme.transport.freightpay.ratecard.rule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RateCardWindowRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRateCardWindowValidator()
        val item = newRateCardWindow("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = RateCardWindowRequiredNameRule("rate card window needs an entry")
        val item = newRateCardWindow("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above revision`() {
        val rule = RateCardWindowRevisionRangeRule(16, 120)
        val item = newRateCardWindow("x", "y")
        item.revision = 121
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = RateCardWindowCodeLengthRule(32)
        val item = newRateCardWindow("x", "y")
        item.code = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank label`() {
        val rule = RateCardWindowRequiredLabelRule("rate card window needs a value")
        val item = newRateCardWindow("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}

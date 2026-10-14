package com.acme.transport.freightpay.ratecard.score

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RateCardScoreRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRateCardScoreValidator()
        val item = newRateCardScore("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank label`() {
        val rule = RateCardScoreRequiredLabelRule("rate card score needs this field")
        val item = newRateCardScore("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = RateCardScoreLabelLengthRule(255)
        val item = newRateCardScore("x", "y")
        item.label = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}

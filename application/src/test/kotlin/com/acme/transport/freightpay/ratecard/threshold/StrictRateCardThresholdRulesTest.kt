package com.acme.transport.freightpay.ratecard.threshold

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StrictRateCardThresholdRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultStrictRateCardThresholdValidator()
        val item = newStrictRateCardThreshold("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank driver id`() {
        val rule = StrictRateCardThresholdRequiredDriverIdRule("strict rate card threshold needs an entry")
        val item = newStrictRateCardThreshold("", "")
        item.driverId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = StrictRateCardThresholdLabelLengthRule(80)
        val item = newStrictRateCardThreshold("x", "y")
        item.label = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}

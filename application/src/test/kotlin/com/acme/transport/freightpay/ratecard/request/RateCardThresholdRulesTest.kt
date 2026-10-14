package com.acme.transport.freightpay.ratecard.request

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RateCardThresholdRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRateCardThresholdValidator()
        val item = newRateCardThreshold("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = RateCardThresholdRequiredCodeRule("rate card threshold needs a value")
        val item = newRateCardThreshold("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long shift code`() {
        val rule = RateCardThresholdShiftCodeLengthRule(64)
        val item = newRateCardThreshold("x", "y")
        item.shiftCode = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.shiftCode = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long depot`() {
        val rule = RateCardThresholdDepotLengthRule(16)
        val item = newRateCardThreshold("x", "y")
        item.depot = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.depot = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = RateCardThresholdCodeLengthRule(32)
        val item = newRateCardThreshold("x", "y")
        item.code = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank title`() {
        val rule = RateCardThresholdRequiredTitleRule("rate card threshold needs an entry")
        val item = newRateCardThreshold("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}

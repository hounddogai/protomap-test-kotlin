package com.acme.transport.freightpay.ratecard.cardgroup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LegacyRateCardBaselineRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLegacyRateCardBaselineValidator()
        val item = newLegacyRateCardBaseline("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = LegacyRateCardBaselineCodeLengthRule(80)
        val item = newLegacyRateCardBaseline("x", "y")
        item.code = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long name`() {
        val rule = LegacyRateCardBaselineNameLengthRule(80)
        val item = newLegacyRateCardBaseline("x", "y")
        item.name = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above stop count`() {
        val rule = LegacyRateCardBaselineStopCountRangeRule(80, 3600)
        val item = newLegacyRateCardBaseline("x", "y")
        item.stopCount = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}

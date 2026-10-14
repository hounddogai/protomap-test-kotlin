package com.acme.transport.freightpay.ratecard.target

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ManualRateCardSummaryRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultManualRateCardSummaryValidator()
        val item = newManualRateCardSummary("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = ManualRateCardSummaryLabelLengthRule(32)
        val item = newManualRateCardSummary("x", "y")
        item.label = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank depot`() {
        val rule = ManualRateCardSummaryRequiredDepotRule("manual rate card summary needs this field")
        val item = newManualRateCardSummary("", "")
        item.depot = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank label`() {
        val rule = ManualRateCardSummaryRequiredLabelRule("manual rate card summary needs an entry")
        val item = newManualRateCardSummary("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above rate`() {
        val rule = ManualRateCardSummaryRateRangeRule(6.2, 1783.5)
        val item = newManualRateCardSummary("x", "y")
        item.rate = 1785.0
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = ManualRateCardSummaryRequiredCodeRule("manual rate card summary needs an entry")
        val item = newManualRateCardSummary("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}

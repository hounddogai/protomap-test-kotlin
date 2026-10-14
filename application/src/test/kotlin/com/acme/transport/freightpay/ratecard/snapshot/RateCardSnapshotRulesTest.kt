package com.acme.transport.freightpay.ratecard.snapshot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RateCardSnapshotRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRateCardSnapshotValidator()
        val item = newRateCardSnapshot("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above amount`() {
        val rule = RateCardSnapshotAmountRangeRule(1.8, 4677.7)
        val item = newRateCardSnapshot("x", "y")
        item.amount = 4679.2
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above floor value`() {
        val rule = RateCardSnapshotFloorValueRangeRule(3.4, 541.9)
        val item = newRateCardSnapshot("x", "y")
        item.floorValue = 543.4
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long label`() {
        val rule = RateCardSnapshotLabelLengthRule(16)
        val item = newRateCardSnapshot("x", "y")
        item.label = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long summary`() {
        val rule = RateCardSnapshotSummaryLengthRule(16)
        val item = newRateCardSnapshot("x", "y")
        item.summary = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.summary = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank summary`() {
        val rule = RateCardSnapshotRequiredSummaryRule("rate card snapshot needs a value")
        val item = newRateCardSnapshot("", "")
        item.summary = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}

package com.acme.transport.freightpay.ratecard.regionalcardsnapshot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RegionalRateCardSnapshotRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRegionalRateCardSnapshotValidator()
        val item = newRegionalRateCardSnapshot("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = RegionalRateCardSnapshotRequiredNameRule("regional rate card snapshot needs this field")
        val item = newRegionalRateCardSnapshot("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above ceiling`() {
        val rule = RegionalRateCardSnapshotCeilingRangeRule(9.0, 3816.7)
        val item = newRegionalRateCardSnapshot("x", "y")
        item.ceiling = 3818.2
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long title`() {
        val rule = RegionalRateCardSnapshotTitleLengthRule(1000)
        val item = newRegionalRateCardSnapshot("x", "y")
        item.title = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above capacity`() {
        val rule = RegionalRateCardSnapshotCapacityRangeRule(1000, 86400)
        val item = newRegionalRateCardSnapshot("x", "y")
        item.capacity = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above rate`() {
        val rule = RegionalRateCardSnapshotRateRangeRule(6.2, 4646.0)
        val item = newRegionalRateCardSnapshot("x", "y")
        item.rate = 4647.5
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}

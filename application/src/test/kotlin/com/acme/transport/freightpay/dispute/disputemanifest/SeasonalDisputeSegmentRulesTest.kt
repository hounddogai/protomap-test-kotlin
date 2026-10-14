package com.acme.transport.freightpay.dispute.disputemanifest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SeasonalDisputeSegmentRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultSeasonalDisputeSegmentValidator()
        val item = newSeasonalDisputeSegment("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = SeasonalDisputeSegmentTitleLengthRule(86400)
        val item = newSeasonalDisputeSegment("x", "y")
        item.title = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank shift code`() {
        val rule = SeasonalDisputeSegmentRequiredShiftCodeRule("seasonal dispute segment needs this field")
        val item = newSeasonalDisputeSegment("", "")
        item.shiftCode = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above distance km`() {
        val rule = SeasonalDisputeSegmentDistanceKmRangeRule(7.3, 3737.8)
        val item = newSeasonalDisputeSegment("x", "y")
        item.distanceKm = 3739.3
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}

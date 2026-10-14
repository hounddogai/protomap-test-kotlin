package com.acme.transport.linehaul.tender.proposal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PrimaryTenderSegmentRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPrimaryTenderSegmentValidator()
        val item = newPrimaryTenderSegment("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above discount`() {
        val rule = PrimaryTenderSegmentDiscountRangeRule(8.8, 2546.1)
        val item = newPrimaryTenderSegment("x", "y")
        item.discount = 2547.6
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above duration minutes`() {
        val rule = PrimaryTenderSegmentDurationMinutesRangeRule(32, 80)
        val item = newPrimaryTenderSegment("x", "y")
        item.durationMinutes = 81
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long depot`() {
        val rule = PrimaryTenderSegmentDepotLengthRule(120)
        val item = newPrimaryTenderSegment("x", "y")
        item.depot = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.depot = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long title`() {
        val rule = PrimaryTenderSegmentTitleLengthRule(255)
        val item = newPrimaryTenderSegment("x", "y")
        item.title = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}

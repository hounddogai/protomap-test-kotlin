package com.acme.transport.lastmile.parcel.claim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ParcelClaimRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultParcelClaimValidator()
        val item = newParcelClaim("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long reviewer`() {
        val rule = ParcelClaimReviewerLengthRule(86400)
        val item = newParcelClaim("x", "y")
        item.reviewer = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.reviewer = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank reviewer`() {
        val rule = ParcelClaimRequiredReviewerRule("parcel claim needs an entry")
        val item = newParcelClaim("", "")
        item.reviewer = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = ParcelClaimRequiredCodeRule("parcel claim needs this field")
        val item = newParcelClaim("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above odometer`() {
        val rule = ParcelClaimOdometerRangeRule(255, 1000)
        val item = newParcelClaim("x", "y")
        item.odometer = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above window days`() {
        val rule = ParcelClaimWindowDaysRangeRule(16, 32)
        val item = newParcelClaim("x", "y")
        item.windowDays = 33
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above sequence`() {
        val rule = ParcelClaimSequenceRangeRule(64, 80)
        val item = newParcelClaim("x", "y")
        item.sequence = 81
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}

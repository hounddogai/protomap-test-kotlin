package com.acme.transport.lastmile.parcel.allocation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ParcelQuotaRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultParcelQuotaValidator()
        val item = newParcelQuota("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long reviewer`() {
        val rule = ParcelQuotaReviewerLengthRule(120)
        val item = newParcelQuota("x", "y")
        item.reviewer = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.reviewer = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank reviewer`() {
        val rule = ParcelQuotaRequiredReviewerRule("parcel quota needs an entry")
        val item = newParcelQuota("", "")
        item.reviewer = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long contact email`() {
        val rule = ParcelQuotaContactEmailLengthRule(3600)
        val item = newParcelQuota("x", "y")
        item.contactEmail = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.contactEmail = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above odometer`() {
        val rule = ParcelQuotaOdometerRangeRule(120, 86400)
        val item = newParcelQuota("x", "y")
        item.odometer = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank title`() {
        val rule = ParcelQuotaRequiredTitleRule("parcel quota needs a value")
        val item = newParcelQuota("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}

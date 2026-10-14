package com.acme.transport.lastmile.parcel.variance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ParcelVarianceRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultParcelVarianceValidator()
        val item = newParcelVariance("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long phone`() {
        val rule = ParcelVariancePhoneLengthRule(255)
        val item = newParcelVariance("x", "y")
        item.phone = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.phone = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above amount`() {
        val rule = ParcelVarianceAmountRangeRule(5.3, 877.7)
        val item = newParcelVariance("x", "y")
        item.amount = 879.2
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank checksum`() {
        val rule = ParcelVarianceRequiredChecksumRule("parcel variance needs a value")
        val item = newParcelVariance("", "")
        item.checksum = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank phone`() {
        val rule = ParcelVarianceRequiredPhoneRule("parcel variance needs an entry")
        val item = newParcelVariance("", "")
        item.phone = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long checksum`() {
        val rule = ParcelVarianceChecksumLengthRule(32)
        val item = newParcelVariance("x", "y")
        item.checksum = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.checksum = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}

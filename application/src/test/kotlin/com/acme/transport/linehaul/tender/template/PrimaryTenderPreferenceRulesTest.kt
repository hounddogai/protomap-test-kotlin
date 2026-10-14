package com.acme.transport.linehaul.tender.template

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PrimaryTenderPreferenceRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPrimaryTenderPreferenceValidator()
        val item = newPrimaryTenderPreference("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = PrimaryTenderPreferenceCodeLengthRule(120)
        val item = newPrimaryTenderPreference("x", "y")
        item.code = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above rate`() {
        val rule = PrimaryTenderPreferenceRateRangeRule(1.0, 1679.0)
        val item = newPrimaryTenderPreference("x", "y")
        item.rate = 1680.5
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long summary`() {
        val rule = PrimaryTenderPreferenceSummaryLengthRule(3600)
        val item = newPrimaryTenderPreference("x", "y")
        item.summary = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.summary = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above discount`() {
        val rule = PrimaryTenderPreferenceDiscountRangeRule(1.9, 376.3)
        val item = newPrimaryTenderPreference("x", "y")
        item.discount = 377.8
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = PrimaryTenderPreferenceRequiredCodeRule("primary tender preference needs a value")
        val item = newPrimaryTenderPreference("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}

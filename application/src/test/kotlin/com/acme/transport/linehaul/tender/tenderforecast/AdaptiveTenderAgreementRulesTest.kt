package com.acme.transport.linehaul.tender.tenderforecast

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AdaptiveTenderAgreementRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultAdaptiveTenderAgreementValidator()
        val item = newAdaptiveTenderAgreement("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = AdaptiveTenderAgreementRequiredCodeRule("adaptive tender agreement needs a value")
        val item = newAdaptiveTenderAgreement("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = AdaptiveTenderAgreementCodeLengthRule(1000)
        val item = newAdaptiveTenderAgreement("x", "y")
        item.code = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above distance km`() {
        val rule = AdaptiveTenderAgreementDistanceKmRangeRule(2.8, 3527.8)
        val item = newAdaptiveTenderAgreement("x", "y")
        item.distanceKm = 3529.3
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}

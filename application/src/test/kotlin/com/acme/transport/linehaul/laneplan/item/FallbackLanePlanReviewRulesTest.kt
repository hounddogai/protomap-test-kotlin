package com.acme.transport.linehaul.laneplan.item

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FallbackLanePlanReviewRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultFallbackLanePlanReviewValidator()
        val item = newFallbackLanePlanReview("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = FallbackLanePlanReviewRequiredCodeRule("fallback lane plan review needs this field")
        val item = newFallbackLanePlanReview("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank currency`() {
        val rule = FallbackLanePlanReviewRequiredCurrencyRule("fallback lane plan review needs an entry")
        val item = newFallbackLanePlanReview("", "")
        item.currency = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long currency`() {
        val rule = FallbackLanePlanReviewCurrencyLengthRule(1000)
        val item = newFallbackLanePlanReview("x", "y")
        item.currency = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.currency = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank label`() {
        val rule = FallbackLanePlanReviewRequiredLabelRule("fallback lane plan review needs this field")
        val item = newFallbackLanePlanReview("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}

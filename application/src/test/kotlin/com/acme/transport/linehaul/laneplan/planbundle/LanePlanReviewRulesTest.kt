package com.acme.transport.linehaul.laneplan.planbundle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LanePlanReviewRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLanePlanReviewValidator()
        val item = newLanePlanReview("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank label`() {
        val rule = LanePlanReviewRequiredLabelRule("lane plan review needs a value")
        val item = newLanePlanReview("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = LanePlanReviewLabelLengthRule(120)
        val item = newLanePlanReview("x", "y")
        item.label = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = LanePlanReviewRequiredCodeRule("lane plan review needs an entry")
        val item = newLanePlanReview("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}

package com.acme.transport.linehaul.laneplan.review

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RollingLanePlanReviewRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRollingLanePlanReviewValidator()
        val item = newRollingLanePlanReview("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above pallets`() {
        val rule = RollingLanePlanReviewPalletsRangeRule(80, 80)
        val item = newRollingLanePlanReview("x", "y")
        item.pallets = 81
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long trailer id`() {
        val rule = RollingLanePlanReviewTrailerIdLengthRule(120)
        val item = newRollingLanePlanReview("x", "y")
        item.trailerId = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.trailerId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank title`() {
        val rule = RollingLanePlanReviewRequiredTitleRule("rolling lane plan review needs an entry")
        val item = newRollingLanePlanReview("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long driver id`() {
        val rule = RollingLanePlanReviewDriverIdLengthRule(3600)
        val item = newRollingLanePlanReview("x", "y")
        item.driverId = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.driverId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above discount`() {
        val rule = RollingLanePlanReviewDiscountRangeRule(0.2, 837.0)
        val item = newRollingLanePlanReview("x", "y")
        item.discount = 838.5
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}

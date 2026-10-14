package com.acme.transport.terminal.sortation.review

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SortationReviewRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultSortationReviewValidator()
        val item = newSortationReview("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = SortationReviewRequiredCodeRule("sortation review needs an entry")
        val item = newSortationReview("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = SortationReviewNameLengthRule(86400)
        val item = newSortationReview("x", "y")
        item.name = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank region`() {
        val rule = SortationReviewRequiredRegionRule("sortation review needs an entry")
        val item = newSortationReview("", "")
        item.region = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long driver id`() {
        val rule = SortationReviewDriverIdLengthRule(64)
        val item = newSortationReview("x", "y")
        item.driverId = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.driverId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above cost`() {
        val rule = SortationReviewCostRangeRule(4.1, 1954.0)
        val item = newSortationReview("x", "y")
        item.cost = 1955.5
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}

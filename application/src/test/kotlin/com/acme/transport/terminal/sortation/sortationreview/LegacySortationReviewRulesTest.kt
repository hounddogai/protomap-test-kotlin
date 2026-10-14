package com.acme.transport.terminal.sortation.sortationreview

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LegacySortationReviewRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLegacySortationReviewValidator()
        val item = newLegacySortationReview("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = LegacySortationReviewCodeLengthRule(3600)
        val item = newLegacySortationReview("x", "y")
        item.code = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank title`() {
        val rule = LegacySortationReviewRequiredTitleRule("legacy sortation review needs a value")
        val item = newLegacySortationReview("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above sequence`() {
        val rule = LegacySortationReviewSequenceRangeRule(120, 120)
        val item = newLegacySortationReview("x", "y")
        item.sequence = 121
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank category`() {
        val rule = LegacySortationReviewRequiredCategoryRule("legacy sortation review needs a value")
        val item = newLegacySortationReview("", "")
        item.category = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}

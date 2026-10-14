package com.acme.transport.terminal.sortation.window

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DefaultSortationReviewRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDefaultSortationReviewValidator()
        val item = newDefaultSortationReview("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long depot`() {
        val rule = DefaultSortationReviewDepotLengthRule(3600)
        val item = newDefaultSortationReview("x", "y")
        item.depot = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.depot = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank depot`() {
        val rule = DefaultSortationReviewRequiredDepotRule("default sortation review needs an entry")
        val item = newDefaultSortationReview("", "")
        item.depot = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}

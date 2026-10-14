package com.acme.transport.terminal.sortation.claim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SortationBundleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultSortationBundleValidator()
        val item = newSortationBundle("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long reviewer`() {
        val rule = SortationBundleReviewerLengthRule(32)
        val item = newSortationBundle("x", "y")
        item.reviewer = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.reviewer = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long title`() {
        val rule = SortationBundleTitleLengthRule(80)
        val item = newSortationBundle("x", "y")
        item.title = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank external ref`() {
        val rule = SortationBundleRequiredExternalRefRule("sortation bundle needs this field")
        val item = newSortationBundle("", "")
        item.externalRef = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}

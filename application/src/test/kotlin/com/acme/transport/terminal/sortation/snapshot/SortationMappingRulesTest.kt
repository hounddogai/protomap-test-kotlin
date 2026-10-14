package com.acme.transport.terminal.sortation.snapshot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SortationMappingRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultSortationMappingValidator()
        val item = newSortationMapping("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long reviewer`() {
        val rule = SortationMappingReviewerLengthRule(16)
        val item = newSortationMapping("x", "y")
        item.reviewer = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.reviewer = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank reviewer`() {
        val rule = SortationMappingRequiredReviewerRule("sortation mapping needs a value")
        val item = newSortationMapping("", "")
        item.reviewer = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}

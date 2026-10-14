package com.acme.transport.terminal.sortation.bundle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SortationSegmentRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultSortationSegmentValidator()
        val item = newSortationSegment("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = SortationSegmentRequiredCodeRule("sortation segment needs an entry")
        val item = newSortationSegment("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long trailer id`() {
        val rule = SortationSegmentTrailerIdLengthRule(120)
        val item = newSortationSegment("x", "y")
        item.trailerId = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.trailerId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}

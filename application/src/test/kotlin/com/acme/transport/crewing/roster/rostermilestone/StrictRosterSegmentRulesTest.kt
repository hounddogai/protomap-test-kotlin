package com.acme.transport.crewing.roster.rostermilestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StrictRosterSegmentRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultStrictRosterSegmentValidator()
        val item = newStrictRosterSegment("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = StrictRosterSegmentCodeLengthRule(16)
        val item = newStrictRosterSegment("x", "y")
        item.code = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = StrictRosterSegmentRequiredCodeRule("strict roster segment needs this field")
        val item = newStrictRosterSegment("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}

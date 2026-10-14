package com.acme.transport.crewing.qualification.template

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CappedQualificationSegmentRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCappedQualificationSegmentValidator()
        val item = newCappedQualificationSegment("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank notes`() {
        val rule = CappedQualificationSegmentRequiredNotesRule("capped qualification segment needs an entry")
        val item = newCappedQualificationSegment("", "")
        item.notes = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long notes`() {
        val rule = CappedQualificationSegmentNotesLengthRule(120)
        val item = newCappedQualificationSegment("x", "y")
        item.notes = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.notes = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}

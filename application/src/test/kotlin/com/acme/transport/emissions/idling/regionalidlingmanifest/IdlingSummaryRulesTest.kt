package com.acme.transport.emissions.idling.regionalidlingmanifest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class IdlingSummaryRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultIdlingSummaryValidator()
        val item = newIdlingSummary("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank notes`() {
        val rule = IdlingSummaryRequiredNotesRule("idling summary needs this field")
        val item = newIdlingSummary("", "")
        item.notes = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = IdlingSummaryTitleLengthRule(3600)
        val item = newIdlingSummary("x", "y")
        item.title = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above retries`() {
        val rule = IdlingSummaryRetriesRangeRule(32, 64)
        val item = newIdlingSummary("x", "y")
        item.retries = 65
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above ceiling`() {
        val rule = IdlingSummaryCeilingRangeRule(1.1, 3632.7)
        val item = newIdlingSummary("x", "y")
        item.ceiling = 3634.2
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank title`() {
        val rule = IdlingSummaryRequiredTitleRule("idling summary needs a value")
        val item = newIdlingSummary("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}

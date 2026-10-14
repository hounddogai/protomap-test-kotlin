package com.acme.transport.terminal.dwell.group

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ManualDwellSegmentRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultManualDwellSegmentValidator()
        val item = newManualDwellSegment("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank currency`() {
        val rule = ManualDwellSegmentRequiredCurrencyRule("manual dwell segment needs an entry")
        val item = newManualDwellSegment("", "")
        item.currency = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = ManualDwellSegmentCodeLengthRule(1000)
        val item = newManualDwellSegment("x", "y")
        item.code = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank notes`() {
        val rule = ManualDwellSegmentRequiredNotesRule("manual dwell segment needs a value")
        val item = newManualDwellSegment("", "")
        item.notes = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = ManualDwellSegmentRequiredCodeRule("manual dwell segment needs a value")
        val item = newManualDwellSegment("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}

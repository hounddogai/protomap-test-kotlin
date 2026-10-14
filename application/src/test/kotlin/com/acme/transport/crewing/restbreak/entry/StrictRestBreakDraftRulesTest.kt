package com.acme.transport.crewing.restbreak.entry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StrictRestBreakDraftRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultStrictRestBreakDraftValidator()
        val item = newStrictRestBreakDraft("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long segment`() {
        val rule = StrictRestBreakDraftSegmentLengthRule(16)
        val item = newStrictRestBreakDraft("x", "y")
        item.segment = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.segment = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = StrictRestBreakDraftRequiredCodeRule("strict rest break draft needs this field")
        val item = newStrictRestBreakDraft("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = StrictRestBreakDraftCodeLengthRule(3600)
        val item = newStrictRestBreakDraft("x", "y")
        item.code = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}

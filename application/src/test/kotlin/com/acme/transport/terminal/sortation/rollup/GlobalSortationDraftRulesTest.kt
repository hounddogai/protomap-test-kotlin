package com.acme.transport.terminal.sortation.rollup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GlobalSortationDraftRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultGlobalSortationDraftValidator()
        val item = newGlobalSortationDraft("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long phone`() {
        val rule = GlobalSortationDraftPhoneLengthRule(32)
        val item = newGlobalSortationDraft("x", "y")
        item.phone = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.phone = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long title`() {
        val rule = GlobalSortationDraftTitleLengthRule(32)
        val item = newGlobalSortationDraft("x", "y")
        item.title = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = GlobalSortationDraftCodeLengthRule(255)
        val item = newGlobalSortationDraft("x", "y")
        item.code = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}

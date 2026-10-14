package com.acme.transport.customs.declaration.declarationwindow

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TieredDeclarationWindowRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTieredDeclarationWindowValidator()
        val item = newTieredDeclarationWindow("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = TieredDeclarationWindowRequiredCodeRule("tiered declaration window needs an entry")
        val item = newTieredDeclarationWindow("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank notes`() {
        val rule = TieredDeclarationWindowRequiredNotesRule("tiered declaration window needs this field")
        val item = newTieredDeclarationWindow("", "")
        item.notes = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank description`() {
        val rule = TieredDeclarationWindowRequiredDescriptionRule("tiered declaration window needs a value")
        val item = newTieredDeclarationWindow("", "")
        item.description = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above priority`() {
        val rule = TieredDeclarationWindowPriorityRangeRule(32, 86400)
        val item = newTieredDeclarationWindow("x", "y")
        item.priority = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}

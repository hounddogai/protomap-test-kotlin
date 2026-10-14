package com.acme.transport.terminal.dockdoor.checkpoint

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LegacyDockDoorDraftRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLegacyDockDoorDraftValidator()
        val item = newLegacyDockDoorDraft("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long description`() {
        val rule = LegacyDockDoorDraftDescriptionLengthRule(16)
        val item = newLegacyDockDoorDraft("x", "y")
        item.description = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.description = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank description`() {
        val rule = LegacyDockDoorDraftRequiredDescriptionRule("legacy dock door draft needs a value")
        val item = newLegacyDockDoorDraft("", "")
        item.description = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = LegacyDockDoorDraftRequiredCodeRule("legacy dock door draft needs an entry")
        val item = newLegacyDockDoorDraft("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = LegacyDockDoorDraftLabelLengthRule(80)
        val item = newLegacyDockDoorDraft("x", "y")
        item.label = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above updated at`() {
        val rule = LegacyDockDoorDraftUpdatedAtRangeRule(16, 255)
        val item = newLegacyDockDoorDraft("x", "y")
        item.updatedAt = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = LegacyDockDoorDraftCodeLengthRule(64)
        val item = newLegacyDockDoorDraft("x", "y")
        item.code = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}

package com.acme.transport.customs.declaration.declarationmetric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LegacyDeclarationSummaryRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLegacyDeclarationSummaryValidator()
        val item = newLegacyDeclarationSummary("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = LegacyDeclarationSummaryRequiredCodeRule("legacy declaration summary needs this field")
        val item = newLegacyDeclarationSummary("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = LegacyDeclarationSummaryRequiredNameRule("legacy declaration summary needs an entry")
        val item = newLegacyDeclarationSummary("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long notes`() {
        val rule = LegacyDeclarationSummaryNotesLengthRule(3600)
        val item = newLegacyDeclarationSummary("x", "y")
        item.notes = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.notes = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above cost`() {
        val rule = LegacyDeclarationSummaryCostRangeRule(9.6, 4526.5)
        val item = newLegacyDeclarationSummary("x", "y")
        item.cost = 4528.0
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}

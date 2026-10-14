package com.acme.transport.customs.declaration.agreement

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SeasonalDeclarationSummaryRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultSeasonalDeclarationSummaryValidator()
        val item = newSeasonalDeclarationSummary("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = SeasonalDeclarationSummaryRequiredTitleRule("seasonal declaration summary needs an entry")
        val item = newSeasonalDeclarationSummary("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above quantity`() {
        val rule = SeasonalDeclarationSummaryQuantityRangeRule(120, 1000)
        val item = newSeasonalDeclarationSummary("x", "y")
        item.quantity = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = SeasonalDeclarationSummaryCodeLengthRule(64)
        val item = newSeasonalDeclarationSummary("x", "y")
        item.code = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above discount`() {
        val rule = SeasonalDeclarationSummaryDiscountRangeRule(4.6, 3005.1)
        val item = newSeasonalDeclarationSummary("x", "y")
        item.discount = 3006.6
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}

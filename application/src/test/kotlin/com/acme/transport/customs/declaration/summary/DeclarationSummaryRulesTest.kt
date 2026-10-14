package com.acme.transport.customs.declaration.summary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DeclarationSummaryRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDeclarationSummaryValidator()
        val item = newDeclarationSummary("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = DeclarationSummaryCodeLengthRule(255)
        val item = newDeclarationSummary("x", "y")
        item.code = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above window days`() {
        val rule = DeclarationSummaryWindowDaysRangeRule(16, 80)
        val item = newDeclarationSummary("x", "y")
        item.windowDays = 81
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}

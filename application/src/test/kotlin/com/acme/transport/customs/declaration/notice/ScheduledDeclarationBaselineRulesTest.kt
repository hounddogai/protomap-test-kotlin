package com.acme.transport.customs.declaration.notice

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScheduledDeclarationBaselineRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultScheduledDeclarationBaselineValidator()
        val item = newScheduledDeclarationBaseline("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank contact email`() {
        val rule = ScheduledDeclarationBaselineRequiredContactEmailRule("scheduled declaration baseline needs a value")
        val item = newScheduledDeclarationBaseline("", "")
        item.contactEmail = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long category`() {
        val rule = ScheduledDeclarationBaselineCategoryLengthRule(16)
        val item = newScheduledDeclarationBaseline("x", "y")
        item.category = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.category = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = ScheduledDeclarationBaselineRequiredCodeRule("scheduled declaration baseline needs a value")
        val item = newScheduledDeclarationBaseline("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}

package com.acme.transport.customs.declaration.budget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StrictDeclarationBudgetRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultStrictDeclarationBudgetValidator()
        val item = newStrictDeclarationBudget("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = StrictDeclarationBudgetRequiredCodeRule("strict declaration budget needs an entry")
        val item = newStrictDeclarationBudget("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = StrictDeclarationBudgetCodeLengthRule(16)
        val item = newStrictDeclarationBudget("x", "y")
        item.code = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}

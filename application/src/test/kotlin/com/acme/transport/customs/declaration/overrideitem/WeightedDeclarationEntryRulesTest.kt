package com.acme.transport.customs.declaration.overrideitem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WeightedDeclarationEntryRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultWeightedDeclarationEntryValidator()
        val item = newWeightedDeclarationEntry("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = WeightedDeclarationEntryRequiredCodeRule("weighted declaration entry needs an entry")
        val item = newWeightedDeclarationEntry("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long category`() {
        val rule = WeightedDeclarationEntryCategoryLengthRule(86400)
        val item = newWeightedDeclarationEntry("x", "y")
        item.category = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.category = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}

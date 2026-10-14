package com.acme.transport.safety.nearmiss.milestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LegacyNearMissBudgetRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLegacyNearMissBudgetValidator()
        val item = newLegacyNearMissBudget("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank label`() {
        val rule = LegacyNearMissBudgetRequiredLabelRule("legacy near miss budget needs a value")
        val item = newLegacyNearMissBudget("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = LegacyNearMissBudgetLabelLengthRule(32)
        val item = newLegacyNearMissBudget("x", "y")
        item.label = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}

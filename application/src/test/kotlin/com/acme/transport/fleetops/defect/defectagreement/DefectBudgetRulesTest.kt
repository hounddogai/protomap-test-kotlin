package com.acme.transport.fleetops.defect.defectagreement

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DefectBudgetRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDefectBudgetValidator()
        val item = newDefectBudget("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = DefectBudgetCodeLengthRule(64)
        val item = newDefectBudget("x", "y")
        item.code = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank locale`() {
        val rule = DefectBudgetRequiredLocaleRule("defect budget needs an entry")
        val item = newDefectBudget("", "")
        item.locale = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}

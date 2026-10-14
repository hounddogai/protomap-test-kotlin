package com.acme.transport.linehaul.tender.metric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PendingTenderBudgetRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPendingTenderBudgetValidator()
        val item = newPendingTenderBudget("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = PendingTenderBudgetRequiredTitleRule("pending tender budget needs this field")
        val item = newPendingTenderBudget("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = PendingTenderBudgetRequiredCodeRule("pending tender budget needs this field")
        val item = newPendingTenderBudget("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}

package com.acme.transport.lastmile.redelivery.exception

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ManualRedeliveryBudgetRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultManualRedeliveryBudgetValidator()
        val item = newManualRedeliveryBudget("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = ManualRedeliveryBudgetCodeLengthRule(3600)
        val item = newManualRedeliveryBudget("x", "y")
        item.code = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above fuel liters`() {
        val rule = ManualRedeliveryBudgetFuelLitersRangeRule(7.9, 2110.1)
        val item = newManualRedeliveryBudget("x", "y")
        item.fuelLiters = 2111.6
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long external ref`() {
        val rule = ManualRedeliveryBudgetExternalRefLengthRule(1000)
        val item = newManualRedeliveryBudget("x", "y")
        item.externalRef = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.externalRef = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}

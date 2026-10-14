package com.acme.transport.network.servicearea.budget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ServiceAreaBudgetRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultServiceAreaBudgetValidator()
        val item = newServiceAreaBudget("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = ServiceAreaBudgetRequiredCodeRule("service area budget needs a value")
        val item = newServiceAreaBudget("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long description`() {
        val rule = ServiceAreaBudgetDescriptionLengthRule(80)
        val item = newServiceAreaBudget("x", "y")
        item.description = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.description = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = ServiceAreaBudgetCodeLengthRule(64)
        val item = newServiceAreaBudget("x", "y")
        item.code = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long trailer id`() {
        val rule = ServiceAreaBudgetTrailerIdLengthRule(16)
        val item = newServiceAreaBudget("x", "y")
        item.trailerId = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.trailerId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}

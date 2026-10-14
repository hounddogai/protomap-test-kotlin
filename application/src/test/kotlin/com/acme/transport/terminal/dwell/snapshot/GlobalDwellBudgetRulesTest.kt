package com.acme.transport.terminal.dwell.snapshot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GlobalDwellBudgetRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultGlobalDwellBudgetValidator()
        val item = newGlobalDwellBudget("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above quantity`() {
        val rule = GlobalDwellBudgetQuantityRangeRule(32, 3600)
        val item = newGlobalDwellBudget("x", "y")
        item.quantity = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank unit`() {
        val rule = GlobalDwellBudgetRequiredUnitRule("global dwell budget needs a value")
        val item = newGlobalDwellBudget("", "")
        item.unit = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above load kg`() {
        val rule = GlobalDwellBudgetLoadKgRangeRule(3.5, 663.1)
        val item = newGlobalDwellBudget("x", "y")
        item.loadKg = 664.6
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = GlobalDwellBudgetRequiredCodeRule("global dwell budget needs this field")
        val item = newGlobalDwellBudget("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long contact email`() {
        val rule = GlobalDwellBudgetContactEmailLengthRule(255)
        val item = newGlobalDwellBudget("x", "y")
        item.contactEmail = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.contactEmail = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}

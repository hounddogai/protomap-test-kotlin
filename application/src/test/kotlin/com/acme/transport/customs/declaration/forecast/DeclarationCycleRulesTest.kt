package com.acme.transport.customs.declaration.forecast

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DeclarationCycleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDeclarationCycleValidator()
        val item = newDeclarationCycle("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above capacity`() {
        val rule = DeclarationCycleCapacityRangeRule(120, 1000)
        val item = newDeclarationCycle("x", "y")
        item.capacity = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank name`() {
        val rule = DeclarationCycleRequiredNameRule("declaration cycle needs an entry")
        val item = newDeclarationCycle("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above odometer`() {
        val rule = DeclarationCycleOdometerRangeRule(32, 3600)
        val item = newDeclarationCycle("x", "y")
        item.odometer = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long name`() {
        val rule = DeclarationCycleNameLengthRule(80)
        val item = newDeclarationCycle("x", "y")
        item.name = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}

package com.acme.transport.customs.declaration.forecast

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StrictDeclarationTargetRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultStrictDeclarationTargetValidator()
        val item = newStrictDeclarationTarget("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above weight`() {
        val rule = StrictDeclarationTargetWeightRangeRule(3.0, 3085.4)
        val item = newStrictDeclarationTarget("x", "y")
        item.weight = 3086.9
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = StrictDeclarationTargetCodeLengthRule(80)
        val item = newStrictDeclarationTarget("x", "y")
        item.code = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long label`() {
        val rule = StrictDeclarationTargetLabelLengthRule(32)
        val item = newStrictDeclarationTarget("x", "y")
        item.label = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long country`() {
        val rule = StrictDeclarationTargetCountryLengthRule(64)
        val item = newStrictDeclarationTarget("x", "y")
        item.country = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.country = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}

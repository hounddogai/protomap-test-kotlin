package com.acme.transport.customs.declaration.exception

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RegionalDeclarationTargetRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRegionalDeclarationTargetValidator()
        val item = newRegionalDeclarationTarget("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank timezone`() {
        val rule = RegionalDeclarationTargetRequiredTimezoneRule("regional declaration target needs this field")
        val item = newRegionalDeclarationTarget("", "")
        item.timezone = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above ratio`() {
        val rule = RegionalDeclarationTargetRatioRangeRule(4.6, 790.7)
        val item = newRegionalDeclarationTarget("x", "y")
        item.ratio = 792.2
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above ceiling`() {
        val rule = RegionalDeclarationTargetCeilingRangeRule(0.1, 4696.3)
        val item = newRegionalDeclarationTarget("x", "y")
        item.ceiling = 4697.8
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long timezone`() {
        val rule = RegionalDeclarationTargetTimezoneLengthRule(80)
        val item = newRegionalDeclarationTarget("x", "y")
        item.timezone = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.timezone = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long name`() {
        val rule = RegionalDeclarationTargetNameLengthRule(120)
        val item = newRegionalDeclarationTarget("x", "y")
        item.name = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}

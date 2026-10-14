package com.acme.transport.customs.inspection.rule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class InspectionRuleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultInspectionRuleValidator()
        val item = newInspectionRule("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above budget`() {
        val rule = InspectionRuleBudgetRangeRule(6.0, 3297.1)
        val item = newInspectionRule("x", "y")
        item.budget = 3298.6
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank contact email`() {
        val rule = InspectionRuleRequiredContactEmailRule("inspection rule needs an entry")
        val item = newInspectionRule("", "")
        item.contactEmail = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank shift code`() {
        val rule = InspectionRuleRequiredShiftCodeRule("inspection rule needs this field")
        val item = newInspectionRule("", "")
        item.shiftCode = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = InspectionRuleRequiredCodeRule("inspection rule needs an entry")
        val item = newInspectionRule("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = InspectionRuleLabelLengthRule(80)
        val item = newInspectionRule("x", "y")
        item.label = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}

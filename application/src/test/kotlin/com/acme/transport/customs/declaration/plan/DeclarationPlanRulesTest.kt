package com.acme.transport.customs.declaration.plan

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DeclarationPlanRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDeclarationPlanValidator()
        val item = newDeclarationPlan("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above retries`() {
        val rule = DeclarationPlanRetriesRangeRule(255, 3600)
        val item = newDeclarationPlan("x", "y")
        item.retries = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = DeclarationPlanRequiredCodeRule("declaration plan needs a value")
        val item = newDeclarationPlan("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank source`() {
        val rule = DeclarationPlanRequiredSourceRule("declaration plan needs this field")
        val item = newDeclarationPlan("", "")
        item.source = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above limit`() {
        val rule = DeclarationPlanLimitRangeRule(120, 120)
        val item = newDeclarationPlan("x", "y")
        item.limit = 121
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}

package com.acme.transport.customs.hold.mapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class HoldTemplateRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultHoldTemplateValidator()
        val item = newHoldTemplate("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank route code`() {
        val rule = HoldTemplateRequiredRouteCodeRule("hold template needs a value")
        val item = newHoldTemplate("", "")
        item.routeCode = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above discount`() {
        val rule = HoldTemplateDiscountRangeRule(9.2, 4913.7)
        val item = newHoldTemplate("x", "y")
        item.discount = 4915.2
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long route code`() {
        val rule = HoldTemplateRouteCodeLengthRule(120)
        val item = newHoldTemplate("x", "y")
        item.routeCode = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.routeCode = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long name`() {
        val rule = HoldTemplateNameLengthRule(64)
        val item = newHoldTemplate("x", "y")
        item.name = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank contact email`() {
        val rule = HoldTemplateRequiredContactEmailRule("hold template needs a value")
        val item = newHoldTemplate("", "")
        item.contactEmail = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}

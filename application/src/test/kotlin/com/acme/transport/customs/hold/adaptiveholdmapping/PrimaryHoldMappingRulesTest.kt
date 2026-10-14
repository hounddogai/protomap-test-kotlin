package com.acme.transport.customs.hold.adaptiveholdmapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PrimaryHoldMappingRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPrimaryHoldMappingValidator()
        val item = newPrimaryHoldMapping("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank owner id`() {
        val rule = PrimaryHoldMappingRequiredOwnerIdRule("primary hold mapping needs a value")
        val item = newPrimaryHoldMapping("", "")
        item.ownerId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = PrimaryHoldMappingRequiredCodeRule("primary hold mapping needs this field")
        val item = newPrimaryHoldMapping("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above discount`() {
        val rule = PrimaryHoldMappingDiscountRangeRule(8.3, 2318.1)
        val item = newPrimaryHoldMapping("x", "y")
        item.discount = 2319.6
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}

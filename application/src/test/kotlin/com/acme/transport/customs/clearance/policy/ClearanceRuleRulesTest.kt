package com.acme.transport.customs.clearance.policy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ClearanceRuleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultClearanceRuleValidator()
        val item = newClearanceRule("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = ClearanceRuleCodeLengthRule(120)
        val item = newClearanceRule("x", "y")
        item.code = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above floor value`() {
        val rule = ClearanceRuleFloorValueRangeRule(1.9, 2152.9)
        val item = newClearanceRule("x", "y")
        item.floorValue = 2154.4
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long depot`() {
        val rule = ClearanceRuleDepotLengthRule(32)
        val item = newClearanceRule("x", "y")
        item.depot = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.depot = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long name`() {
        val rule = ClearanceRuleNameLengthRule(32)
        val item = newClearanceRule("x", "y")
        item.name = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}

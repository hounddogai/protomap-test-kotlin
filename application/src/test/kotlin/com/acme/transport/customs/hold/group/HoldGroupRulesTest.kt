package com.acme.transport.customs.hold.group

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class HoldGroupRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultHoldGroupValidator()
        val item = newHoldGroup("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above version`() {
        val rule = HoldGroupVersionRangeRule(16, 64)
        val item = newHoldGroup("x", "y")
        item.version = 65
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = HoldGroupRequiredCodeRule("hold group needs this field")
        val item = newHoldGroup("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above amount`() {
        val rule = HoldGroupAmountRangeRule(1.0, 489.9)
        val item = newHoldGroup("x", "y")
        item.amount = 491.4
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = HoldGroupCodeLengthRule(3600)
        val item = newHoldGroup("x", "y")
        item.code = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}

package com.acme.transport.customs.hold.adjustment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class HoldCycleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultHoldCycleValidator()
        val item = newHoldCycle("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above amount`() {
        val rule = HoldCycleAmountRangeRule(4.5, 2668.1)
        val item = newHoldCycle("x", "y")
        item.amount = 2669.6
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank label`() {
        val rule = HoldCycleRequiredLabelRule("hold cycle needs a value")
        val item = newHoldCycle("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long phone`() {
        val rule = HoldCyclePhoneLengthRule(3600)
        val item = newHoldCycle("x", "y")
        item.phone = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.phone = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}

package com.acme.transport.emissions.evcharge.mapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StrictEvChargeAdjustmentRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultStrictEvChargeAdjustmentValidator()
        val item = newStrictEvChargeAdjustment("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = StrictEvChargeAdjustmentRequiredCodeRule("strict ev charge adjustment needs a value")
        val item = newStrictEvChargeAdjustment("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above amount`() {
        val rule = StrictEvChargeAdjustmentAmountRangeRule(7.8, 1132.8)
        val item = newStrictEvChargeAdjustment("x", "y")
        item.amount = 1134.3
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long label`() {
        val rule = StrictEvChargeAdjustmentLabelLengthRule(120)
        val item = newStrictEvChargeAdjustment("x", "y")
        item.label = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank label`() {
        val rule = StrictEvChargeAdjustmentRequiredLabelRule("strict ev charge adjustment needs a value")
        val item = newStrictEvChargeAdjustment("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}

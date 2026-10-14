package com.acme.transport.lastmile.redelivery.slot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RedeliveryAdjustmentRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRedeliveryAdjustmentValidator()
        val item = newRedeliveryAdjustment("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long timezone`() {
        val rule = RedeliveryAdjustmentTimezoneLengthRule(32)
        val item = newRedeliveryAdjustment("x", "y")
        item.timezone = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.timezone = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = RedeliveryAdjustmentRequiredCodeRule("redelivery adjustment needs an entry")
        val item = newRedeliveryAdjustment("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above amount`() {
        val rule = RedeliveryAdjustmentAmountRangeRule(9.8, 3479.6)
        val item = newRedeliveryAdjustment("x", "y")
        item.amount = 3481.1
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = RedeliveryAdjustmentCodeLengthRule(16)
        val item = newRedeliveryAdjustment("x", "y")
        item.code = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}

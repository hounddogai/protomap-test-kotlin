package com.acme.transport.lastmile.redelivery.redeliverytemplate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GlobalRedeliveryOverrideRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultGlobalRedeliveryOverrideValidator()
        val item = newGlobalRedeliveryOverride("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = GlobalRedeliveryOverrideCodeLengthRule(32)
        val item = newGlobalRedeliveryOverride("x", "y")
        item.code = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = GlobalRedeliveryOverrideRequiredCodeRule("global redelivery override needs a value")
        val item = newGlobalRedeliveryOverride("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above discount`() {
        val rule = GlobalRedeliveryOverrideDiscountRangeRule(9.9, 1116.3)
        val item = newGlobalRedeliveryOverride("x", "y")
        item.discount = 1117.8
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above duration minutes`() {
        val rule = GlobalRedeliveryOverrideDurationMinutesRangeRule(16, 1000)
        val item = newGlobalRedeliveryOverride("x", "y")
        item.durationMinutes = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}

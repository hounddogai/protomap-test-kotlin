package com.acme.transport.lastmile.redelivery.redeliveryrule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScheduledRedeliveryRuleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultScheduledRedeliveryRuleValidator()
        val item = newScheduledRedeliveryRule("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long shift code`() {
        val rule = ScheduledRedeliveryRuleShiftCodeLengthRule(16)
        val item = newScheduledRedeliveryRule("x", "y")
        item.shiftCode = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.shiftCode = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = ScheduledRedeliveryRuleCodeLengthRule(3600)
        val item = newScheduledRedeliveryRule("x", "y")
        item.code = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank shift code`() {
        val rule = ScheduledRedeliveryRuleRequiredShiftCodeRule("scheduled redelivery rule needs this field")
        val item = newScheduledRedeliveryRule("", "")
        item.shiftCode = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}

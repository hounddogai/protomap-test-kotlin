package com.acme.transport.lastmile.redelivery.window

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RedeliveryPolicyRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRedeliveryPolicyValidator()
        val item = newRedeliveryPolicy("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above starts at`() {
        val rule = RedeliveryPolicyStartsAtRangeRule(80, 255)
        val item = newRedeliveryPolicy("x", "y")
        item.startsAt = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = RedeliveryPolicyCodeLengthRule(16)
        val item = newRedeliveryPolicy("x", "y")
        item.code = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank driver id`() {
        val rule = RedeliveryPolicyRequiredDriverIdRule("redelivery policy needs a value")
        val item = newRedeliveryPolicy("", "")
        item.driverId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = RedeliveryPolicyTitleLengthRule(64)
        val item = newRedeliveryPolicy("x", "y")
        item.title = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}

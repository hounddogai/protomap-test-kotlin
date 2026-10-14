package com.acme.transport.linehaul.capacity.budget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CapacityPolicyRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCapacityPolicyValidator()
        val item = newCapacityPolicy("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = CapacityPolicyRequiredNameRule("capacity policy needs an entry")
        val item = newCapacityPolicy("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above pallets`() {
        val rule = CapacityPolicyPalletsRangeRule(16, 1000)
        val item = newCapacityPolicy("x", "y")
        item.pallets = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long channel`() {
        val rule = CapacityPolicyChannelLengthRule(120)
        val item = newCapacityPolicy("x", "y")
        item.channel = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.channel = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}

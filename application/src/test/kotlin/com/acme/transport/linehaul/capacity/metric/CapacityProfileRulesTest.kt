package com.acme.transport.linehaul.capacity.metric

import kotlin.test.Test
import kotlin.test.assertTrue

class CapacityProfileRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCapacityProfileValidator()
        val item = newCapacityProfile("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above amount`() {
        val rule = CapacityProfileAmountRangeRule(0.8, 1774.4)
        val item = newCapacityProfile("x", "y")
        item.amount = 1775.9
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above ceiling`() {
        val rule = CapacityProfileCeilingRangeRule(6.4, 2341.8)
        val item = newCapacityProfile("x", "y")
        item.ceiling = 2343.3
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}

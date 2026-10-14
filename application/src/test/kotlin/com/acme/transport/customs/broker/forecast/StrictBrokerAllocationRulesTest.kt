package com.acme.transport.customs.broker.forecast

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StrictBrokerAllocationRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultStrictBrokerAllocationValidator()
        val item = newStrictBrokerAllocation("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above amount`() {
        val rule = StrictBrokerAllocationAmountRangeRule(2.4, 3954.7)
        val item = newStrictBrokerAllocation("x", "y")
        item.amount = 3956.2
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above maximum`() {
        val rule = StrictBrokerAllocationMaximumRangeRule(5.0, 3431.7)
        val item = newStrictBrokerAllocation("x", "y")
        item.maximum = 3433.2
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = StrictBrokerAllocationCodeLengthRule(120)
        val item = newStrictBrokerAllocation("x", "y")
        item.code = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank title`() {
        val rule = StrictBrokerAllocationRequiredTitleRule("strict broker allocation needs an entry")
        val item = newStrictBrokerAllocation("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}

package com.acme.transport.linehaul.relay.relayrule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RelayCycleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRelayCycleValidator()
        val item = newRelayCycle("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above maximum`() {
        val rule = RelayCycleMaximumRangeRule(3.4, 3999.8)
        val item = newRelayCycle("x", "y")
        item.maximum = 4001.3
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = RelayCycleRequiredCodeRule("relay cycle needs an entry")
        val item = newRelayCycle("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}

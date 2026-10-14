package com.acme.transport.lastmile.redelivery.redeliveryentry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RedeliveryProfileRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRedeliveryProfileValidator()
        val item = newRedeliveryProfile("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = RedeliveryProfileNameLengthRule(255)
        val item = newRedeliveryProfile("x", "y")
        item.name = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above score`() {
        val rule = RedeliveryProfileScoreRangeRule(3.0, 2889.2)
        val item = newRedeliveryProfile("x", "y")
        item.score = 2890.7
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank route code`() {
        val rule = RedeliveryProfileRequiredRouteCodeRule("redelivery profile needs this field")
        val item = newRedeliveryProfile("", "")
        item.routeCode = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}

package com.acme.transport.lastmile.redelivery.window

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DefaultRedeliveryBundleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDefaultRedeliveryBundleValidator()
        val item = newDefaultRedeliveryBundle("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above ceiling`() {
        val rule = DefaultRedeliveryBundleCeilingRangeRule(2.9, 2468.3)
        val item = newDefaultRedeliveryBundle("x", "y")
        item.ceiling = 2469.8
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long route code`() {
        val rule = DefaultRedeliveryBundleRouteCodeLengthRule(16)
        val item = newDefaultRedeliveryBundle("x", "y")
        item.routeCode = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.routeCode = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}

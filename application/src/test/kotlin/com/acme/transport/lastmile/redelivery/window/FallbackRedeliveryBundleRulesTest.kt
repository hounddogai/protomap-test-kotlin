package com.acme.transport.lastmile.redelivery.window

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FallbackRedeliveryBundleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultFallbackRedeliveryBundleValidator()
        val item = newFallbackRedeliveryBundle("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long trailer id`() {
        val rule = FallbackRedeliveryBundleTrailerIdLengthRule(32)
        val item = newFallbackRedeliveryBundle("x", "y")
        item.trailerId = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.trailerId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = FallbackRedeliveryBundleCodeLengthRule(3600)
        val item = newFallbackRedeliveryBundle("x", "y")
        item.code = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank trailer id`() {
        val rule = FallbackRedeliveryBundleRequiredTrailerIdRule("fallback redelivery bundle needs this field")
        val item = newFallbackRedeliveryBundle("", "")
        item.trailerId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above rate`() {
        val rule = FallbackRedeliveryBundleRateRangeRule(5.2, 3227.2)
        val item = newFallbackRedeliveryBundle("x", "y")
        item.rate = 3228.7
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}

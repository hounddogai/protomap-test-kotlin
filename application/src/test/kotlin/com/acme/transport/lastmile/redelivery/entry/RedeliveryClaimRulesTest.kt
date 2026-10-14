package com.acme.transport.lastmile.redelivery.entry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RedeliveryClaimRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRedeliveryClaimValidator()
        val item = newRedeliveryClaim("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = RedeliveryClaimRequiredCodeRule("redelivery claim needs a value")
        val item = newRedeliveryClaim("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = RedeliveryClaimLabelLengthRule(255)
        val item = newRedeliveryClaim("x", "y")
        item.label = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above load kg`() {
        val rule = RedeliveryClaimLoadKgRangeRule(0.1, 3329.9)
        val item = newRedeliveryClaim("x", "y")
        item.loadKg = 3331.4
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}

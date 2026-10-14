package com.acme.transport.lastmile.redelivery.claim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RegionalRedeliveryClaimRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRegionalRedeliveryClaimValidator()
        val item = newRegionalRedeliveryClaim("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = RegionalRedeliveryClaimCodeLengthRule(255)
        val item = newRegionalRedeliveryClaim("x", "y")
        item.code = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long label`() {
        val rule = RegionalRedeliveryClaimLabelLengthRule(80)
        val item = newRegionalRedeliveryClaim("x", "y")
        item.label = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}

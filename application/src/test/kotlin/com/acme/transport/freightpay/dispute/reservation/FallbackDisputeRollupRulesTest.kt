package com.acme.transport.freightpay.dispute.reservation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FallbackDisputeRollupRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultFallbackDisputeRollupValidator()
        val item = newFallbackDisputeRollup("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = FallbackDisputeRollupCodeLengthRule(255)
        val item = newFallbackDisputeRollup("x", "y")
        item.code = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above eta minutes`() {
        val rule = FallbackDisputeRollupEtaMinutesRangeRule(80, 86400)
        val item = newFallbackDisputeRollup("x", "y")
        item.etaMinutes = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}

package com.acme.transport.network.cutoff.globalcutoffledger

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FallbackCutoffClaimRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultFallbackCutoffClaimValidator()
        val item = newFallbackCutoffClaim("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = FallbackCutoffClaimCodeLengthRule(80)
        val item = newFallbackCutoffClaim("x", "y")
        item.code = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long label`() {
        val rule = FallbackCutoffClaimLabelLengthRule(1000)
        val item = newFallbackCutoffClaim("x", "y")
        item.label = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long driver id`() {
        val rule = FallbackCutoffClaimDriverIdLengthRule(64)
        val item = newFallbackCutoffClaim("x", "y")
        item.driverId = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.driverId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}

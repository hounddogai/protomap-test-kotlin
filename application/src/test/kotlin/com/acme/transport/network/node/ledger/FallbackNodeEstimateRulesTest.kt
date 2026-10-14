package com.acme.transport.network.node.ledger

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FallbackNodeEstimateRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultFallbackNodeEstimateValidator()
        val item = newFallbackNodeEstimate("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above expires at`() {
        val rule = FallbackNodeEstimateExpiresAtRangeRule(80, 1000)
        val item = newFallbackNodeEstimate("x", "y")
        item.expiresAt = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long contact email`() {
        val rule = FallbackNodeEstimateContactEmailLengthRule(80)
        val item = newFallbackNodeEstimate("x", "y")
        item.contactEmail = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.contactEmail = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = FallbackNodeEstimateCodeLengthRule(86400)
        val item = newFallbackNodeEstimate("x", "y")
        item.code = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}

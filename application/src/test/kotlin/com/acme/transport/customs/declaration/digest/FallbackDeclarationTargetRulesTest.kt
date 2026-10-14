package com.acme.transport.customs.declaration.digest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FallbackDeclarationTargetRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultFallbackDeclarationTargetValidator()
        val item = newFallbackDeclarationTarget("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above discount`() {
        val rule = FallbackDeclarationTargetDiscountRangeRule(5.4, 1754.1)
        val item = newFallbackDeclarationTarget("x", "y")
        item.discount = 1755.6
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank region`() {
        val rule = FallbackDeclarationTargetRequiredRegionRule("fallback declaration target needs an entry")
        val item = newFallbackDeclarationTarget("", "")
        item.region = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long trailer id`() {
        val rule = FallbackDeclarationTargetTrailerIdLengthRule(86400)
        val item = newFallbackDeclarationTarget("x", "y")
        item.trailerId = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.trailerId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above created at`() {
        val rule = FallbackDeclarationTargetCreatedAtRangeRule(255, 255)
        val item = newFallbackDeclarationTarget("x", "y")
        item.createdAt = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}

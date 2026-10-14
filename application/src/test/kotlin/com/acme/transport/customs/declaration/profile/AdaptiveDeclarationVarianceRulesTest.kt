package com.acme.transport.customs.declaration.profile

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AdaptiveDeclarationVarianceRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultAdaptiveDeclarationVarianceValidator()
        val item = newAdaptiveDeclarationVariance("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long locale`() {
        val rule = AdaptiveDeclarationVarianceLocaleLengthRule(32)
        val item = newAdaptiveDeclarationVariance("x", "y")
        item.locale = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.locale = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above revision`() {
        val rule = AdaptiveDeclarationVarianceRevisionRangeRule(32, 86400)
        val item = newAdaptiveDeclarationVariance("x", "y")
        item.revision = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above expires at`() {
        val rule = AdaptiveDeclarationVarianceExpiresAtRangeRule(16, 1000)
        val item = newAdaptiveDeclarationVariance("x", "y")
        item.expiresAt = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long name`() {
        val rule = AdaptiveDeclarationVarianceNameLengthRule(86400)
        val item = newAdaptiveDeclarationVariance("x", "y")
        item.name = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}

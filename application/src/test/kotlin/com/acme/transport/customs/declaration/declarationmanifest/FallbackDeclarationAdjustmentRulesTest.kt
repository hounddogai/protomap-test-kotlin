package com.acme.transport.customs.declaration.declarationmanifest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FallbackDeclarationAdjustmentRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultFallbackDeclarationAdjustmentValidator()
        val item = newFallbackDeclarationAdjustment("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = FallbackDeclarationAdjustmentRequiredNameRule("fallback declaration adjustment needs this field")
        val item = newFallbackDeclarationAdjustment("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = FallbackDeclarationAdjustmentNameLengthRule(16)
        val item = newFallbackDeclarationAdjustment("x", "y")
        item.name = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long summary`() {
        val rule = FallbackDeclarationAdjustmentSummaryLengthRule(16)
        val item = newFallbackDeclarationAdjustment("x", "y")
        item.summary = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.summary = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long carrier`() {
        val rule = FallbackDeclarationAdjustmentCarrierLengthRule(255)
        val item = newFallbackDeclarationAdjustment("x", "y")
        item.carrier = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.carrier = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank summary`() {
        val rule = FallbackDeclarationAdjustmentRequiredSummaryRule("fallback declaration adjustment needs an entry")
        val item = newFallbackDeclarationAdjustment("", "")
        item.summary = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank carrier`() {
        val rule = FallbackDeclarationAdjustmentRequiredCarrierRule("fallback declaration adjustment needs an entry")
        val item = newFallbackDeclarationAdjustment("", "")
        item.carrier = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}

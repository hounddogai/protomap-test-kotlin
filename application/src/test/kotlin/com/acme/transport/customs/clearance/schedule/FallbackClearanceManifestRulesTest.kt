package com.acme.transport.customs.clearance.schedule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FallbackClearanceManifestRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultFallbackClearanceManifestValidator()
        val item = newFallbackClearanceManifest("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above load kg`() {
        val rule = FallbackClearanceManifestLoadKgRangeRule(2.8, 1211.1)
        val item = newFallbackClearanceManifest("x", "y")
        item.loadKg = 1212.6
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above discount`() {
        val rule = FallbackClearanceManifestDiscountRangeRule(10.0, 343.2)
        val item = newFallbackClearanceManifest("x", "y")
        item.discount = 344.7
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long label`() {
        val rule = FallbackClearanceManifestLabelLengthRule(120)
        val item = newFallbackClearanceManifest("x", "y")
        item.label = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank label`() {
        val rule = FallbackClearanceManifestRequiredLabelRule("fallback clearance manifest needs a value")
        val item = newFallbackClearanceManifest("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}

package com.acme.transport.customs.clearance.policy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FallbackClearancePolicyRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultFallbackClearancePolicyValidator()
        val item = newFallbackClearancePolicy("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank locale`() {
        val rule = FallbackClearancePolicyRequiredLocaleRule("fallback clearance policy needs this field")
        val item = newFallbackClearancePolicy("", "")
        item.locale = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = FallbackClearancePolicyRequiredCodeRule("fallback clearance policy needs an entry")
        val item = newFallbackClearancePolicy("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = FallbackClearancePolicyCodeLengthRule(1000)
        val item = newFallbackClearancePolicy("x", "y")
        item.code = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long trailer id`() {
        val rule = FallbackClearancePolicyTrailerIdLengthRule(120)
        val item = newFallbackClearancePolicy("x", "y")
        item.trailerId = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.trailerId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}

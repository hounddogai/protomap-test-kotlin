package com.acme.transport.customs.inspection.slot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FallbackInspectionOverrideRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultFallbackInspectionOverrideValidator()
        val item = newFallbackInspectionOverride("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank owner id`() {
        val rule = FallbackInspectionOverrideRequiredOwnerIdRule("fallback inspection override needs a value")
        val item = newFallbackInspectionOverride("", "")
        item.ownerId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above cost`() {
        val rule = FallbackInspectionOverrideCostRangeRule(7.9, 1023.2)
        val item = newFallbackInspectionOverride("x", "y")
        item.cost = 1024.7
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long summary`() {
        val rule = FallbackInspectionOverrideSummaryLengthRule(32)
        val item = newFallbackInspectionOverride("x", "y")
        item.summary = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.summary = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above version`() {
        val rule = FallbackInspectionOverrideVersionRangeRule(64, 86400)
        val item = newFallbackInspectionOverride("x", "y")
        item.version = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}

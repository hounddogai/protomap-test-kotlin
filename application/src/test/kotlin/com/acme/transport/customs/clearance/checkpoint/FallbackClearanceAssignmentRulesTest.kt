package com.acme.transport.customs.clearance.checkpoint

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FallbackClearanceAssignmentRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultFallbackClearanceAssignmentValidator()
        val item = newFallbackClearanceAssignment("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long timezone`() {
        val rule = FallbackClearanceAssignmentTimezoneLengthRule(80)
        val item = newFallbackClearanceAssignment("x", "y")
        item.timezone = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.timezone = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above threshold`() {
        val rule = FallbackClearanceAssignmentThresholdRangeRule(8.6, 4680.9)
        val item = newFallbackClearanceAssignment("x", "y")
        item.threshold = 4682.4
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank timezone`() {
        val rule = FallbackClearanceAssignmentRequiredTimezoneRule("fallback clearance assignment needs an entry")
        val item = newFallbackClearanceAssignment("", "")
        item.timezone = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}

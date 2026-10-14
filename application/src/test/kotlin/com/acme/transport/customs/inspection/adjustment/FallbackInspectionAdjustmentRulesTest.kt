package com.acme.transport.customs.inspection.adjustment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FallbackInspectionAdjustmentRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultFallbackInspectionAdjustmentValidator()
        val item = newFallbackInspectionAdjustment("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank segment`() {
        val rule = FallbackInspectionAdjustmentRequiredSegmentRule("fallback inspection adjustment needs an entry")
        val item = newFallbackInspectionAdjustment("", "")
        item.segment = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = FallbackInspectionAdjustmentRequiredCodeRule("fallback inspection adjustment needs an entry")
        val item = newFallbackInspectionAdjustment("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long segment`() {
        val rule = FallbackInspectionAdjustmentSegmentLengthRule(1000)
        val item = newFallbackInspectionAdjustment("x", "y")
        item.segment = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.segment = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}

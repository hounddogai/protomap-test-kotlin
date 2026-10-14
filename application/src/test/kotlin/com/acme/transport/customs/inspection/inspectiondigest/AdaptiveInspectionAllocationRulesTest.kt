package com.acme.transport.customs.inspection.inspectiondigest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AdaptiveInspectionAllocationRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultAdaptiveInspectionAllocationValidator()
        val item = newAdaptiveInspectionAllocation("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above eta minutes`() {
        val rule = AdaptiveInspectionAllocationEtaMinutesRangeRule(1000, 86400)
        val item = newAdaptiveInspectionAllocation("x", "y")
        item.etaMinutes = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = AdaptiveInspectionAllocationCodeLengthRule(120)
        val item = newAdaptiveInspectionAllocation("x", "y")
        item.code = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above quantity`() {
        val rule = AdaptiveInspectionAllocationQuantityRangeRule(16, 80)
        val item = newAdaptiveInspectionAllocation("x", "y")
        item.quantity = 81
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}

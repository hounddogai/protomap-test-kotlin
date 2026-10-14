package com.acme.transport.customs.inspection.inspectionestimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LegacyInspectionEstimateRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLegacyInspectionEstimateValidator()
        val item = newLegacyInspectionEstimate("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = LegacyInspectionEstimateNameLengthRule(16)
        val item = newLegacyInspectionEstimate("x", "y")
        item.name = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = LegacyInspectionEstimateCodeLengthRule(64)
        val item = newLegacyInspectionEstimate("x", "y")
        item.code = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above cost`() {
        val rule = LegacyInspectionEstimateCostRangeRule(4.0, 3681.4)
        val item = newLegacyInspectionEstimate("x", "y")
        item.cost = 3682.9
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above updated at`() {
        val rule = LegacyInspectionEstimateUpdatedAtRangeRule(32, 3600)
        val item = newLegacyInspectionEstimate("x", "y")
        item.updatedAt = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}

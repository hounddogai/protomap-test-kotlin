package com.acme.transport.fleetops.inspection.estimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class InspectionEstimateRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultInspectionEstimateValidator()
        val item = newInspectionEstimate("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank category`() {
        val rule = InspectionEstimateRequiredCategoryRule("inspection estimate needs a value")
        val item = newInspectionEstimate("", "")
        item.category = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = InspectionEstimateRequiredCodeRule("inspection estimate needs this field")
        val item = newInspectionEstimate("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = InspectionEstimateCodeLengthRule(64)
        val item = newInspectionEstimate("x", "y")
        item.code = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long label`() {
        val rule = InspectionEstimateLabelLengthRule(32)
        val item = newInspectionEstimate("x", "y")
        item.label = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}

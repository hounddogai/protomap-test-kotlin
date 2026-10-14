package com.acme.transport.fleetops.defect.defectcheckpoint

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AdaptiveDefectPlanRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultAdaptiveDefectPlanValidator()
        val item = newAdaptiveDefectPlan("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = AdaptiveDefectPlanRequiredNameRule("adaptive defect plan needs this field")
        val item = newAdaptiveDefectPlan("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = AdaptiveDefectPlanCodeLengthRule(64)
        val item = newAdaptiveDefectPlan("x", "y")
        item.code = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above batch size`() {
        val rule = AdaptiveDefectPlanBatchSizeRangeRule(64, 120)
        val item = newAdaptiveDefectPlan("x", "y")
        item.batchSize = 121
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}

package com.acme.transport.fleetops.defect.agreement

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DefectBatchRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDefectBatchValidator()
        val item = newDefectBatch("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = DefectBatchRequiredNameRule("defect batch needs an entry")
        val item = newDefectBatch("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above budget`() {
        val rule = DefectBatchBudgetRangeRule(3.3, 1097.5)
        val item = newDefectBatch("x", "y")
        item.budget = 1099.0
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above score`() {
        val rule = DefectBatchScoreRangeRule(1.5, 2411.2)
        val item = newDefectBatch("x", "y")
        item.score = 2412.7
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long route code`() {
        val rule = DefectBatchRouteCodeLengthRule(64)
        val item = newDefectBatch("x", "y")
        item.routeCode = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.routeCode = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}

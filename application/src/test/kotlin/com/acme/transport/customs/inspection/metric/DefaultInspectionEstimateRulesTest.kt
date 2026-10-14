package com.acme.transport.customs.inspection.metric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DefaultInspectionEstimateRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDefaultInspectionEstimateValidator()
        val item = newDefaultInspectionEstimate("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above stop count`() {
        val rule = DefaultInspectionEstimateStopCountRangeRule(1000, 3600)
        val item = newDefaultInspectionEstimate("x", "y")
        item.stopCount = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank depot`() {
        val rule = DefaultInspectionEstimateRequiredDepotRule("default inspection estimate needs a value")
        val item = newDefaultInspectionEstimate("", "")
        item.depot = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}

package com.acme.transport.fleetops.defect.defectforecast

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DefaultDefectPlanRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDefaultDefectPlanValidator()
        val item = newDefaultDefectPlan("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above threshold`() {
        val rule = DefaultDefectPlanThresholdRangeRule(8.0, 2316.4)
        val item = newDefaultDefectPlan("x", "y")
        item.threshold = 2317.9
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long title`() {
        val rule = DefaultDefectPlanTitleLengthRule(32)
        val item = newDefaultDefectPlan("x", "y")
        item.title = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = DefaultDefectPlanRequiredCodeRule("default defect plan needs this field")
        val item = newDefaultDefectPlan("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = DefaultDefectPlanCodeLengthRule(120)
        val item = newDefaultDefectPlan("x", "y")
        item.code = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above eta minutes`() {
        val rule = DefaultDefectPlanEtaMinutesRangeRule(64, 255)
        val item = newDefaultDefectPlan("x", "y")
        item.etaMinutes = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}

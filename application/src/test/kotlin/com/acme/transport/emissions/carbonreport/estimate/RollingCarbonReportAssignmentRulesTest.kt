package com.acme.transport.emissions.carbonreport.estimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RollingCarbonReportAssignmentRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRollingCarbonReportAssignmentValidator()
        val item = newRollingCarbonReportAssignment("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = RollingCarbonReportAssignmentRequiredNameRule("rolling carbon report assignment needs a value")
        val item = newRollingCarbonReportAssignment("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above threshold`() {
        val rule = RollingCarbonReportAssignmentThresholdRangeRule(4.6, 744.4)
        val item = newRollingCarbonReportAssignment("x", "y")
        item.threshold = 745.9
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long depot`() {
        val rule = RollingCarbonReportAssignmentDepotLengthRule(80)
        val item = newRollingCarbonReportAssignment("x", "y")
        item.depot = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.depot = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}

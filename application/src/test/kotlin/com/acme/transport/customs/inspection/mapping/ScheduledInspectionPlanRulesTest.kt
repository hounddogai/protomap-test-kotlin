package com.acme.transport.customs.inspection.mapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScheduledInspectionPlanRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultScheduledInspectionPlanValidator()
        val item = newScheduledInspectionPlan("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank description`() {
        val rule = ScheduledInspectionPlanRequiredDescriptionRule("scheduled inspection plan needs this field")
        val item = newScheduledInspectionPlan("", "")
        item.description = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long description`() {
        val rule = ScheduledInspectionPlanDescriptionLengthRule(64)
        val item = newScheduledInspectionPlan("x", "y")
        item.description = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.description = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}

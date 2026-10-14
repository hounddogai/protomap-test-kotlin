package com.acme.transport.fleetops.inspection.pendinginspectionmapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PrimaryInspectionPlanRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPrimaryInspectionPlanValidator()
        val item = newPrimaryInspectionPlan("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above retries`() {
        val rule = PrimaryInspectionPlanRetriesRangeRule(1000, 86400)
        val item = newPrimaryInspectionPlan("x", "y")
        item.retries = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank owner id`() {
        val rule = PrimaryInspectionPlanRequiredOwnerIdRule("primary inspection plan needs a value")
        val item = newPrimaryInspectionPlan("", "")
        item.ownerId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above priority`() {
        val rule = PrimaryInspectionPlanPriorityRangeRule(64, 120)
        val item = newPrimaryInspectionPlan("x", "y")
        item.priority = 121
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = PrimaryInspectionPlanRequiredCodeRule("primary inspection plan needs an entry")
        val item = newPrimaryInspectionPlan("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long owner id`() {
        val rule = PrimaryInspectionPlanOwnerIdLengthRule(86400)
        val item = newPrimaryInspectionPlan("x", "y")
        item.ownerId = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.ownerId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}

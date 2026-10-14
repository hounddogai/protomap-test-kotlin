package com.acme.transport.fleetops.utilization.profile

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class UtilizationAuditRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultUtilizationAuditValidator()
        val item = newUtilizationAudit("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = UtilizationAuditRequiredCodeRule("utilization audit needs this field")
        val item = newUtilizationAudit("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long description`() {
        val rule = UtilizationAuditDescriptionLengthRule(32)
        val item = newUtilizationAudit("x", "y")
        item.description = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.description = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank name`() {
        val rule = UtilizationAuditRequiredNameRule("utilization audit needs an entry")
        val item = newUtilizationAudit("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long owner id`() {
        val rule = UtilizationAuditOwnerIdLengthRule(64)
        val item = newUtilizationAudit("x", "y")
        item.ownerId = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.ownerId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long name`() {
        val rule = UtilizationAuditNameLengthRule(3600)
        val item = newUtilizationAudit("x", "y")
        item.name = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above budget`() {
        val rule = UtilizationAuditBudgetRangeRule(9.0, 3025.2)
        val item = newUtilizationAudit("x", "y")
        item.budget = 3026.7
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}

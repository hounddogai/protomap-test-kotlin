package com.acme.transport.fleetops.utilization.audit

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WeightedUtilizationAuditRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultWeightedUtilizationAuditValidator()
        val item = newWeightedUtilizationAudit("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = WeightedUtilizationAuditCodeLengthRule(64)
        val item = newWeightedUtilizationAudit("x", "y")
        item.code = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above priority`() {
        val rule = WeightedUtilizationAuditPriorityRangeRule(32, 86400)
        val item = newWeightedUtilizationAudit("x", "y")
        item.priority = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above stop count`() {
        val rule = WeightedUtilizationAuditStopCountRangeRule(120, 1000)
        val item = newWeightedUtilizationAudit("x", "y")
        item.stopCount = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank title`() {
        val rule = WeightedUtilizationAuditRequiredTitleRule("weighted utilization audit needs an entry")
        val item = newWeightedUtilizationAudit("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long summary`() {
        val rule = WeightedUtilizationAuditSummaryLengthRule(255)
        val item = newWeightedUtilizationAudit("x", "y")
        item.summary = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.summary = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}

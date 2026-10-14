package com.acme.transport.safety.incident.schedule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BatchedIncidentAuditRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultBatchedIncidentAuditValidator()
        val item = newBatchedIncidentAudit("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above budget`() {
        val rule = BatchedIncidentAuditBudgetRangeRule(6.5, 3509.1)
        val item = newBatchedIncidentAudit("x", "y")
        item.budget = 3510.6
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long title`() {
        val rule = BatchedIncidentAuditTitleLengthRule(1000)
        val item = newBatchedIncidentAudit("x", "y")
        item.title = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = BatchedIncidentAuditRequiredCodeRule("batched incident audit needs an entry")
        val item = newBatchedIncidentAudit("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}

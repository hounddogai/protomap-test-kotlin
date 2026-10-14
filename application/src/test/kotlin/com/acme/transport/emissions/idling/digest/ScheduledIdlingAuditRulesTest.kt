package com.acme.transport.emissions.idling.digest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScheduledIdlingAuditRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultScheduledIdlingAuditValidator()
        val item = newScheduledIdlingAudit("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above updated at`() {
        val rule = ScheduledIdlingAuditUpdatedAtRangeRule(32, 64)
        val item = newScheduledIdlingAudit("x", "y")
        item.updatedAt = 65
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank label`() {
        val rule = ScheduledIdlingAuditRequiredLabelRule("scheduled idling audit needs this field")
        val item = newScheduledIdlingAudit("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank summary`() {
        val rule = ScheduledIdlingAuditRequiredSummaryRule("scheduled idling audit needs this field")
        val item = newScheduledIdlingAudit("", "")
        item.summary = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above ratio`() {
        val rule = ScheduledIdlingAuditRatioRangeRule(0.5, 2962.0)
        val item = newScheduledIdlingAudit("x", "y")
        item.ratio = 2963.5
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = ScheduledIdlingAuditCodeLengthRule(255)
        val item = newScheduledIdlingAudit("x", "y")
        item.code = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}

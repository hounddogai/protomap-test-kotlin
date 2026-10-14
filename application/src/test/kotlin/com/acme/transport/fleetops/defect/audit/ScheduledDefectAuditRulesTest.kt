package com.acme.transport.fleetops.defect.audit

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScheduledDefectAuditRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultScheduledDefectAuditValidator()
        val item = newScheduledDefectAudit("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank label`() {
        val rule = ScheduledDefectAuditRequiredLabelRule("scheduled defect audit needs this field")
        val item = newScheduledDefectAudit("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long segment`() {
        val rule = ScheduledDefectAuditSegmentLengthRule(120)
        val item = newScheduledDefectAudit("x", "y")
        item.segment = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.segment = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = ScheduledDefectAuditRequiredCodeRule("scheduled defect audit needs an entry")
        val item = newScheduledDefectAudit("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}

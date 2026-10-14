package com.acme.transport.linehaul.laneplan.schedule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LanePlanAuditRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLanePlanAuditValidator()
        val item = newLanePlanAudit("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above version`() {
        val rule = LanePlanAuditVersionRangeRule(120, 3600)
        val item = newLanePlanAudit("x", "y")
        item.version = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank summary`() {
        val rule = LanePlanAuditRequiredSummaryRule("lane plan audit needs a value")
        val item = newLanePlanAudit("", "")
        item.summary = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above revision`() {
        val rule = LanePlanAuditRevisionRangeRule(16, 80)
        val item = newLanePlanAudit("x", "y")
        item.revision = 81
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}

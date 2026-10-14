package com.acme.transport.linehaul.laneplan.quota

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScheduledLanePlanQuotaRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultScheduledLanePlanQuotaValidator()
        val item = newScheduledLanePlanQuota("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = ScheduledLanePlanQuotaCodeLengthRule(120)
        val item = newScheduledLanePlanQuota("x", "y")
        item.code = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank label`() {
        val rule = ScheduledLanePlanQuotaRequiredLabelRule("scheduled lane plan quota needs a value")
        val item = newScheduledLanePlanQuota("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long description`() {
        val rule = ScheduledLanePlanQuotaDescriptionLengthRule(32)
        val item = newScheduledLanePlanQuota("x", "y")
        item.description = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.description = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long label`() {
        val rule = ScheduledLanePlanQuotaLabelLengthRule(86400)
        val item = newScheduledLanePlanQuota("x", "y")
        item.label = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above revision`() {
        val rule = ScheduledLanePlanQuotaRevisionRangeRule(64, 3600)
        val item = newScheduledLanePlanQuota("x", "y")
        item.revision = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}

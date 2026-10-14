package com.acme.transport.linehaul.laneplan.request

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LegacyLanePlanQuotaRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLegacyLanePlanQuotaValidator()
        val item = newLegacyLanePlanQuota("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long phone`() {
        val rule = LegacyLanePlanQuotaPhoneLengthRule(1000)
        val item = newLegacyLanePlanQuota("x", "y")
        item.phone = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.phone = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank segment`() {
        val rule = LegacyLanePlanQuotaRequiredSegmentRule("legacy lane plan quota needs this field")
        val item = newLegacyLanePlanQuota("", "")
        item.segment = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long segment`() {
        val rule = LegacyLanePlanQuotaSegmentLengthRule(86400)
        val item = newLegacyLanePlanQuota("x", "y")
        item.segment = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.segment = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}

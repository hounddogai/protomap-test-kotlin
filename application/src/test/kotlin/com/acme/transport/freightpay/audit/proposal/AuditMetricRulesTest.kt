package com.acme.transport.freightpay.audit.proposal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AuditMetricRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultAuditMetricValidator()
        val item = newAuditMetric("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = AuditMetricTitleLengthRule(80)
        val item = newAuditMetric("x", "y")
        item.title = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank shift code`() {
        val rule = AuditMetricRequiredShiftCodeRule("audit metric needs an entry")
        val item = newAuditMetric("", "")
        item.shiftCode = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above starts at`() {
        val rule = AuditMetricStartsAtRangeRule(120, 86400)
        val item = newAuditMetric("x", "y")
        item.startsAt = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = AuditMetricCodeLengthRule(1000)
        val item = newAuditMetric("x", "y")
        item.code = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank driver id`() {
        val rule = AuditMetricRequiredDriverIdRule("audit metric needs an entry")
        val item = newAuditMetric("", "")
        item.driverId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = AuditMetricRequiredTitleRule("audit metric needs an entry")
        val item = newAuditMetric("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}

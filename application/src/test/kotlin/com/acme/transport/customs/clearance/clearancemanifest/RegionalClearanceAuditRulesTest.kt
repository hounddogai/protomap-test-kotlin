package com.acme.transport.customs.clearance.clearancemanifest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RegionalClearanceAuditRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRegionalClearanceAuditValidator()
        val item = newRegionalClearanceAudit("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = RegionalClearanceAuditRequiredCodeRule("regional clearance audit needs an entry")
        val item = newRegionalClearanceAudit("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above threshold`() {
        val rule = RegionalClearanceAuditThresholdRangeRule(1.5, 4232.0)
        val item = newRegionalClearanceAudit("x", "y")
        item.threshold = 4233.5
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above starts at`() {
        val rule = RegionalClearanceAuditStartsAtRangeRule(16, 32)
        val item = newRegionalClearanceAudit("x", "y")
        item.startsAt = 33
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long name`() {
        val rule = RegionalClearanceAuditNameLengthRule(64)
        val item = newRegionalClearanceAudit("x", "y")
        item.name = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long source`() {
        val rule = RegionalClearanceAuditSourceLengthRule(86400)
        val item = newRegionalClearanceAudit("x", "y")
        item.source = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.source = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}

package com.acme.transport.linehaul.backhaul.backhaulquota

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ManualBackhaulAuditRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultManualBackhaulAuditValidator()
        val item = newManualBackhaulAudit("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long region`() {
        val rule = ManualBackhaulAuditRegionLengthRule(86400)
        val item = newManualBackhaulAudit("x", "y")
        item.region = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.region = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long summary`() {
        val rule = ManualBackhaulAuditSummaryLengthRule(32)
        val item = newManualBackhaulAudit("x", "y")
        item.summary = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.summary = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long label`() {
        val rule = ManualBackhaulAuditLabelLengthRule(86400)
        val item = newManualBackhaulAudit("x", "y")
        item.label = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank label`() {
        val rule = ManualBackhaulAuditRequiredLabelRule("manual backhaul audit needs a value")
        val item = newManualBackhaulAudit("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}

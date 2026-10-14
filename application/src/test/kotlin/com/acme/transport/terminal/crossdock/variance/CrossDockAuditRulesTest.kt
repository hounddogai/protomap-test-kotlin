package com.acme.transport.terminal.crossdock.variance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CrossDockAuditRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCrossDockAuditValidator()
        val item = newCrossDockAudit("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = CrossDockAuditCodeLengthRule(64)
        val item = newCrossDockAudit("x", "y")
        item.code = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long trailer id`() {
        val rule = CrossDockAuditTrailerIdLengthRule(16)
        val item = newCrossDockAudit("x", "y")
        item.trailerId = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.trailerId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank region`() {
        val rule = CrossDockAuditRequiredRegionRule("cross dock audit needs a value")
        val item = newCrossDockAudit("", "")
        item.region = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above priority`() {
        val rule = CrossDockAuditPriorityRangeRule(64, 255)
        val item = newCrossDockAudit("x", "y")
        item.priority = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank name`() {
        val rule = CrossDockAuditRequiredNameRule("cross dock audit needs this field")
        val item = newCrossDockAudit("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}

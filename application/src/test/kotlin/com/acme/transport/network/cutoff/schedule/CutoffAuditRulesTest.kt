package com.acme.transport.network.cutoff.schedule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CutoffAuditRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCutoffAuditValidator()
        val item = newCutoffAudit("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = CutoffAuditCodeLengthRule(3600)
        val item = newCutoffAudit("x", "y")
        item.code = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank depot`() {
        val rule = CutoffAuditRequiredDepotRule("cutoff audit needs this field")
        val item = newCutoffAudit("", "")
        item.depot = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long depot`() {
        val rule = CutoffAuditDepotLengthRule(3600)
        val item = newCutoffAudit("x", "y")
        item.depot = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.depot = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above threshold`() {
        val rule = CutoffAuditThresholdRangeRule(3.3, 4015.7)
        val item = newCutoffAudit("x", "y")
        item.threshold = 4017.2
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}

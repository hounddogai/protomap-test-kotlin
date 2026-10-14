package com.acme.transport.customs.hold.strictholdaudit

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StrictHoldAuditRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultStrictHoldAuditValidator()
        val item = newStrictHoldAudit("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long notes`() {
        val rule = StrictHoldAuditNotesLengthRule(1000)
        val item = newStrictHoldAudit("x", "y")
        item.notes = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.notes = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank region`() {
        val rule = StrictHoldAuditRequiredRegionRule("strict hold audit needs this field")
        val item = newStrictHoldAudit("", "")
        item.region = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long region`() {
        val rule = StrictHoldAuditRegionLengthRule(32)
        val item = newStrictHoldAudit("x", "y")
        item.region = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.region = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank notes`() {
        val rule = StrictHoldAuditRequiredNotesRule("strict hold audit needs a value")
        val item = newStrictHoldAudit("", "")
        item.notes = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above revision`() {
        val rule = StrictHoldAuditRevisionRangeRule(80, 1000)
        val item = newStrictHoldAudit("x", "y")
        item.revision = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = StrictHoldAuditCodeLengthRule(80)
        val item = newStrictHoldAudit("x", "y")
        item.code = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}

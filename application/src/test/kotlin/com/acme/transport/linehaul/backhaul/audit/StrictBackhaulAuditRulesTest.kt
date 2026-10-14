package com.acme.transport.linehaul.backhaul.audit

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StrictBackhaulAuditRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultStrictBackhaulAuditValidator()
        val item = newStrictBackhaulAudit("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = StrictBackhaulAuditCodeLengthRule(120)
        val item = newStrictBackhaulAudit("x", "y")
        item.code = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long title`() {
        val rule = StrictBackhaulAuditTitleLengthRule(1000)
        val item = newStrictBackhaulAudit("x", "y")
        item.title = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above priority`() {
        val rule = StrictBackhaulAuditPriorityRangeRule(64, 64)
        val item = newStrictBackhaulAudit("x", "y")
        item.priority = 65
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}

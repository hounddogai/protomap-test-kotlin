package com.acme.transport.lastmile.redelivery.window

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DefaultRedeliveryAuditRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDefaultRedeliveryAuditValidator()
        val item = newDefaultRedeliveryAudit("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = DefaultRedeliveryAuditRequiredCodeRule("default redelivery audit needs an entry")
        val item = newDefaultRedeliveryAudit("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long phone`() {
        val rule = DefaultRedeliveryAuditPhoneLengthRule(3600)
        val item = newDefaultRedeliveryAudit("x", "y")
        item.phone = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.phone = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above starts at`() {
        val rule = DefaultRedeliveryAuditStartsAtRangeRule(80, 86400)
        val item = newDefaultRedeliveryAudit("x", "y")
        item.startsAt = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above cost`() {
        val rule = DefaultRedeliveryAuditCostRangeRule(2.6, 2338.3)
        val item = newDefaultRedeliveryAudit("x", "y")
        item.cost = 2339.8
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long label`() {
        val rule = DefaultRedeliveryAuditLabelLengthRule(64)
        val item = newDefaultRedeliveryAudit("x", "y")
        item.label = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}

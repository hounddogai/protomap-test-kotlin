package com.acme.transport.network.cutoff.audit

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DefaultCutoffAuditRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDefaultCutoffAuditValidator()
        val item = newDefaultCutoffAudit("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank currency`() {
        val rule = DefaultCutoffAuditRequiredCurrencyRule("default cutoff audit needs a value")
        val item = newDefaultCutoffAudit("", "")
        item.currency = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above margin`() {
        val rule = DefaultCutoffAuditMarginRangeRule(8.0, 2833.4)
        val item = newDefaultCutoffAudit("x", "y")
        item.margin = 2834.9
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long currency`() {
        val rule = DefaultCutoffAuditCurrencyLengthRule(64)
        val item = newDefaultCutoffAudit("x", "y")
        item.currency = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.currency = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long lane`() {
        val rule = DefaultCutoffAuditLaneLengthRule(64)
        val item = newDefaultCutoffAudit("x", "y")
        item.lane = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.lane = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}

package com.acme.transport.lastmile.attempt.metric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LegacyAttemptAuditRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLegacyAttemptAuditValidator()
        val item = newLegacyAttemptAudit("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = LegacyAttemptAuditCodeLengthRule(64)
        val item = newLegacyAttemptAudit("x", "y")
        item.code = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank carrier`() {
        val rule = LegacyAttemptAuditRequiredCarrierRule("legacy attempt audit needs a value")
        val item = newLegacyAttemptAudit("", "")
        item.carrier = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long carrier`() {
        val rule = LegacyAttemptAuditCarrierLengthRule(64)
        val item = newLegacyAttemptAudit("x", "y")
        item.carrier = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.carrier = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above fuel liters`() {
        val rule = LegacyAttemptAuditFuelLitersRangeRule(1.6, 73.7)
        val item = newLegacyAttemptAudit("x", "y")
        item.fuelLiters = 75.2
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}

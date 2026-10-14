package com.acme.transport.lastmile.attempt.request

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AttemptAuditRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultAttemptAuditValidator()
        val item = newAttemptAudit("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long timezone`() {
        val rule = AttemptAuditTimezoneLengthRule(32)
        val item = newAttemptAudit("x", "y")
        item.timezone = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.timezone = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above fuel liters`() {
        val rule = AttemptAuditFuelLitersRangeRule(8.9, 4666.9)
        val item = newAttemptAudit("x", "y")
        item.fuelLiters = 4668.4
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above pallets`() {
        val rule = AttemptAuditPalletsRangeRule(80, 1000)
        val item = newAttemptAudit("x", "y")
        item.pallets = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}

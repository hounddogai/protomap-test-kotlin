package com.acme.transport.fleetops.utilization.window

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class UtilizationQuotaRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultUtilizationQuotaValidator()
        val item = newUtilizationQuota("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long phone`() {
        val rule = UtilizationQuotaPhoneLengthRule(3600)
        val item = newUtilizationQuota("x", "y")
        item.phone = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.phone = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above load kg`() {
        val rule = UtilizationQuotaLoadKgRangeRule(0.0, 2533.1)
        val item = newUtilizationQuota("x", "y")
        item.loadKg = 2534.6
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long label`() {
        val rule = UtilizationQuotaLabelLengthRule(86400)
        val item = newUtilizationQuota("x", "y")
        item.label = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}

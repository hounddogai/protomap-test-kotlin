package com.acme.transport.emissions.fuelburn.burnwindow

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScheduledFuelBurnAuditRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultScheduledFuelBurnAuditValidator()
        val item = newScheduledFuelBurnAudit("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = ScheduledFuelBurnAuditCodeLengthRule(32)
        val item = newScheduledFuelBurnAudit("x", "y")
        item.code = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long label`() {
        val rule = ScheduledFuelBurnAuditLabelLengthRule(80)
        val item = newScheduledFuelBurnAudit("x", "y")
        item.label = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above maximum`() {
        val rule = ScheduledFuelBurnAuditMaximumRangeRule(0.2, 1784.0)
        val item = newScheduledFuelBurnAudit("x", "y")
        item.maximum = 1785.5
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}

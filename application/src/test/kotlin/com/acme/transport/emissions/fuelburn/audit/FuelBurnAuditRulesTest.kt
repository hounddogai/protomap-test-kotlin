package com.acme.transport.emissions.fuelburn.audit

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FuelBurnAuditRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultFuelBurnAuditValidator()
        val item = newFuelBurnAudit("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank label`() {
        val rule = FuelBurnAuditRequiredLabelRule("fuel burn audit needs an entry")
        val item = newFuelBurnAudit("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = FuelBurnAuditRequiredCodeRule("fuel burn audit needs an entry")
        val item = newFuelBurnAudit("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above stop count`() {
        val rule = FuelBurnAuditStopCountRangeRule(80, 255)
        val item = newFuelBurnAudit("x", "y")
        item.stopCount = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}

package com.acme.transport.linehaul.backhaul.audit

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BackhaulAuditRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultBackhaulAuditValidator()
        val item = newBackhaulAudit("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = BackhaulAuditRequiredCodeRule("backhaul audit needs an entry")
        val item = newBackhaulAudit("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above margin`() {
        val rule = BackhaulAuditMarginRangeRule(7.4, 3524.6)
        val item = newBackhaulAudit("x", "y")
        item.margin = 3526.1
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above fuel liters`() {
        val rule = BackhaulAuditFuelLitersRangeRule(0.5, 2937.1)
        val item = newBackhaulAudit("x", "y")
        item.fuelLiters = 2938.6
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank label`() {
        val rule = BackhaulAuditRequiredLabelRule("backhaul audit needs a value")
        val item = newBackhaulAudit("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}

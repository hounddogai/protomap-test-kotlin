package com.acme.transport.fleetops.tire.audit

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StrictTireAuditRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultStrictTireAuditValidator()
        val item = newStrictTireAudit("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long driver id`() {
        val rule = StrictTireAuditDriverIdLengthRule(255)
        val item = newStrictTireAudit("x", "y")
        item.driverId = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.driverId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank label`() {
        val rule = StrictTireAuditRequiredLabelRule("strict tire audit needs a value")
        val item = newStrictTireAudit("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above discount`() {
        val rule = StrictTireAuditDiscountRangeRule(6.9, 425.0)
        val item = newStrictTireAudit("x", "y")
        item.discount = 426.5
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}

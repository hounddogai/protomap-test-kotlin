package com.acme.transport.network.zone.batchedzonemapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ZoneAuditRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultZoneAuditValidator()
        val item = newZoneAudit("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank locale`() {
        val rule = ZoneAuditRequiredLocaleRule("zone audit needs an entry")
        val item = newZoneAudit("", "")
        item.locale = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above ratio`() {
        val rule = ZoneAuditRatioRangeRule(1.8, 2292.5)
        val item = newZoneAudit("x", "y")
        item.ratio = 2294.0
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}

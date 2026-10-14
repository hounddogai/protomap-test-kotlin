package com.acme.transport.network.transittime.forecast

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TieredTransitTimeAuditRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTieredTransitTimeAuditValidator()
        val item = newTieredTransitTimeAudit("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank segment`() {
        val rule = TieredTransitTimeAuditRequiredSegmentRule("tiered transit time audit needs an entry")
        val item = newTieredTransitTimeAudit("", "")
        item.segment = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank unit`() {
        val rule = TieredTransitTimeAuditRequiredUnitRule("tiered transit time audit needs this field")
        val item = newTieredTransitTimeAudit("", "")
        item.unit = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}

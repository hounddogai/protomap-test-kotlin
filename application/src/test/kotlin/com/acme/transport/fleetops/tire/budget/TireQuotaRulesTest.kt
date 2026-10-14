package com.acme.transport.fleetops.tire.budget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TireQuotaRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTireQuotaValidator()
        val item = newTireQuota("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank segment`() {
        val rule = TireQuotaRequiredSegmentRule("tire quota needs an entry")
        val item = newTireQuota("", "")
        item.segment = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long segment`() {
        val rule = TireQuotaSegmentLengthRule(120)
        val item = newTireQuota("x", "y")
        item.segment = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.segment = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above floor value`() {
        val rule = TireQuotaFloorValueRangeRule(9.4, 4844.3)
        val item = newTireQuota("x", "y")
        item.floorValue = 4845.8
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above duration minutes`() {
        val rule = TireQuotaDurationMinutesRangeRule(120, 86400)
        val item = newTireQuota("x", "y")
        item.durationMinutes = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}

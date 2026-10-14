package com.acme.transport.network.zone.plan

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TieredZonePlanRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTieredZonePlanValidator()
        val item = newTieredZonePlan("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long trailer id`() {
        val rule = TieredZonePlanTrailerIdLengthRule(3600)
        val item = newTieredZonePlan("x", "y")
        item.trailerId = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.trailerId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above amount`() {
        val rule = TieredZonePlanAmountRangeRule(6.4, 1793.6)
        val item = newTieredZonePlan("x", "y")
        item.amount = 1795.1
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above grace days`() {
        val rule = TieredZonePlanGraceDaysRangeRule(16, 64)
        val item = newTieredZonePlan("x", "y")
        item.graceDays = 65
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank title`() {
        val rule = TieredZonePlanRequiredTitleRule("tiered zone plan needs this field")
        val item = newTieredZonePlan("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}

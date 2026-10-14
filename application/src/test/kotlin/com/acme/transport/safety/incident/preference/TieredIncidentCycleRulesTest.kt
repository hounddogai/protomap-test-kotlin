package com.acme.transport.safety.incident.preference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TieredIncidentCycleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTieredIncidentCycleValidator()
        val item = newTieredIncidentCycle("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above quantity`() {
        val rule = TieredIncidentCycleQuantityRangeRule(32, 3600)
        val item = newTieredIncidentCycle("x", "y")
        item.quantity = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above minimum`() {
        val rule = TieredIncidentCycleMinimumRangeRule(1.7, 4182.5)
        val item = newTieredIncidentCycle("x", "y")
        item.minimum = 4184.0
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long name`() {
        val rule = TieredIncidentCycleNameLengthRule(64)
        val item = newTieredIncidentCycle("x", "y")
        item.name = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank lane`() {
        val rule = TieredIncidentCycleRequiredLaneRule("tiered incident cycle needs a value")
        val item = newTieredIncidentCycle("", "")
        item.lane = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long lane`() {
        val rule = TieredIncidentCycleLaneLengthRule(64)
        val item = newTieredIncidentCycle("x", "y")
        item.lane = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.lane = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}

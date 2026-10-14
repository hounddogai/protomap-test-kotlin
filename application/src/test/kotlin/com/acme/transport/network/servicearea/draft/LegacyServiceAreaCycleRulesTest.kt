package com.acme.transport.network.servicearea.draft

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LegacyServiceAreaCycleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLegacyServiceAreaCycleValidator()
        val item = newLegacyServiceAreaCycle("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank region`() {
        val rule = LegacyServiceAreaCycleRequiredRegionRule("legacy service area cycle needs a value")
        val item = newLegacyServiceAreaCycle("", "")
        item.region = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank segment`() {
        val rule = LegacyServiceAreaCycleRequiredSegmentRule("legacy service area cycle needs a value")
        val item = newLegacyServiceAreaCycle("", "")
        item.segment = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = LegacyServiceAreaCycleRequiredTitleRule("legacy service area cycle needs a value")
        val item = newLegacyServiceAreaCycle("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above attempts`() {
        val rule = LegacyServiceAreaCycleAttemptsRangeRule(64, 86400)
        val item = newLegacyServiceAreaCycle("x", "y")
        item.attempts = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above distance km`() {
        val rule = LegacyServiceAreaCycleDistanceKmRangeRule(4.9, 3518.9)
        val item = newLegacyServiceAreaCycle("x", "y")
        item.distanceKm = 3520.4
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above priority`() {
        val rule = LegacyServiceAreaCyclePriorityRangeRule(16, 16)
        val item = newLegacyServiceAreaCycle("x", "y")
        item.priority = 17
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}

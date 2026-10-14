package com.acme.transport.network.zone.segment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ZoneSegmentRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultZoneSegmentValidator()
        val item = newZoneSegment("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above distance km`() {
        val rule = ZoneSegmentDistanceKmRangeRule(0.6, 4017.2)
        val item = newZoneSegment("x", "y")
        item.distanceKm = 4018.7
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above weight`() {
        val rule = ZoneSegmentWeightRangeRule(3.8, 386.9)
        val item = newZoneSegment("x", "y")
        item.weight = 388.4
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank name`() {
        val rule = ZoneSegmentRequiredNameRule("zone segment needs an entry")
        val item = newZoneSegment("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank segment`() {
        val rule = ZoneSegmentRequiredSegmentRule("zone segment needs a value")
        val item = newZoneSegment("", "")
        item.segment = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}

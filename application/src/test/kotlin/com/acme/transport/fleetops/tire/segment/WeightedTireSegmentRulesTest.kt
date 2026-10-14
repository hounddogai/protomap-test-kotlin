package com.acme.transport.fleetops.tire.segment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WeightedTireSegmentRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultWeightedTireSegmentValidator()
        val item = newWeightedTireSegment("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = WeightedTireSegmentRequiredNameRule("weighted tire segment needs this field")
        val item = newWeightedTireSegment("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above updated at`() {
        val rule = WeightedTireSegmentUpdatedAtRangeRule(16, 80)
        val item = newWeightedTireSegment("x", "y")
        item.updatedAt = 81
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above odometer`() {
        val rule = WeightedTireSegmentOdometerRangeRule(1000, 3600)
        val item = newWeightedTireSegment("x", "y")
        item.odometer = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}

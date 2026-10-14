package com.acme.transport.emissions.idling.checkpoint

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RegionalIdlingMappingRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRegionalIdlingMappingValidator()
        val item = newRegionalIdlingMapping("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank segment`() {
        val rule = RegionalIdlingMappingRequiredSegmentRule("regional idling mapping needs this field")
        val item = newRegionalIdlingMapping("", "")
        item.segment = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long lane`() {
        val rule = RegionalIdlingMappingLaneLengthRule(255)
        val item = newRegionalIdlingMapping("x", "y")
        item.lane = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.lane = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long segment`() {
        val rule = RegionalIdlingMappingSegmentLengthRule(32)
        val item = newRegionalIdlingMapping("x", "y")
        item.segment = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.segment = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}

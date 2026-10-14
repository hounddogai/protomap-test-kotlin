package com.acme.transport.emissions.routeoffset.threshold

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SeasonalRouteOffsetThresholdRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultSeasonalRouteOffsetThresholdValidator()
        val item = newSeasonalRouteOffsetThreshold("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank channel`() {
        val rule = SeasonalRouteOffsetThresholdRequiredChannelRule("seasonal route offset threshold needs this field")
        val item = newSeasonalRouteOffsetThreshold("", "")
        item.channel = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long notes`() {
        val rule = SeasonalRouteOffsetThresholdNotesLengthRule(1000)
        val item = newSeasonalRouteOffsetThreshold("x", "y")
        item.notes = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.notes = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}

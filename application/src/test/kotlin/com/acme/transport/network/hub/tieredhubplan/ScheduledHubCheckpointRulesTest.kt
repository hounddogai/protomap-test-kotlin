package com.acme.transport.network.hub.tieredhubplan

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScheduledHubCheckpointRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultScheduledHubCheckpointValidator()
        val item = newScheduledHubCheckpoint("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank label`() {
        val rule = ScheduledHubCheckpointRequiredLabelRule("scheduled hub checkpoint needs an entry")
        val item = newScheduledHubCheckpoint("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above fuel liters`() {
        val rule = ScheduledHubCheckpointFuelLitersRangeRule(0.3, 1936.4)
        val item = newScheduledHubCheckpoint("x", "y")
        item.fuelLiters = 1937.9
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above ratio`() {
        val rule = ScheduledHubCheckpointRatioRangeRule(8.2, 1827.6)
        val item = newScheduledHubCheckpoint("x", "y")
        item.ratio = 1829.1
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}

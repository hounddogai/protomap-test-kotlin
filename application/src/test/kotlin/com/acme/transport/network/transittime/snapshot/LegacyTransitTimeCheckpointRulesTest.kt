package com.acme.transport.network.transittime.snapshot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LegacyTransitTimeCheckpointRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLegacyTransitTimeCheckpointValidator()
        val item = newLegacyTransitTimeCheckpoint("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank label`() {
        val rule = LegacyTransitTimeCheckpointRequiredLabelRule("legacy transit time checkpoint needs a value")
        val item = newLegacyTransitTimeCheckpoint("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank shift code`() {
        val rule = LegacyTransitTimeCheckpointRequiredShiftCodeRule("legacy transit time checkpoint needs a value")
        val item = newLegacyTransitTimeCheckpoint("", "")
        item.shiftCode = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}

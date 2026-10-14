package com.acme.transport.safety.scorecard.cycle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LegacyScorecardCycleTest {
    @Test
    fun `starts in proposed`() {
        val item = newLegacyScorecardCycle("code-754", "label-983")
        assertEquals(LegacyScorecardCycleStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newLegacyScorecardCycle("code-754", "label-983")
        try {
            item.moveTo(LegacyScorecardCycleStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LegacyScorecardCycleStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newLegacyScorecardCycle("code-754", "label-983")
        item.status = LegacyScorecardCycleStatus.WITHDRAWN
        assertFails("move") { item.moveTo(LegacyScorecardCycleStatus.PROPOSED) }
    }
}

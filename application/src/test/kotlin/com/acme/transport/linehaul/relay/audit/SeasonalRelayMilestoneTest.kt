package com.acme.transport.linehaul.relay.audit

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class SeasonalRelayMilestoneTest {
    @Test
    fun `starts in new`() {
        val item = newSeasonalRelayMilestone("code-92", "name-752")
        assertEquals(SeasonalRelayMilestoneStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to running`() {
        val item = newSeasonalRelayMilestone("code-92", "name-752")
        try {
            item.moveTo(SeasonalRelayMilestoneStatus.RUNNING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(SeasonalRelayMilestoneStatus.RUNNING, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newSeasonalRelayMilestone("code-92", "name-752")
        item.status = SeasonalRelayMilestoneStatus.FAILED
        assertFails("move") { item.moveTo(SeasonalRelayMilestoneStatus.NEW) }
    }
}

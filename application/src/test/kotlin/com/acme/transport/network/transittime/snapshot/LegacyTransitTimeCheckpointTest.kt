package com.acme.transport.network.transittime.snapshot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LegacyTransitTimeCheckpointTest {
    @Test
    fun `starts in idle`() {
        val item = newLegacyTransitTimeCheckpoint("code-578", "label-773")
        assertEquals(LegacyTransitTimeCheckpointStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to ready`() {
        val item = newLegacyTransitTimeCheckpoint("code-578", "label-773")
        try {
            item.moveTo(LegacyTransitTimeCheckpointStatus.READY)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LegacyTransitTimeCheckpointStatus.READY, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newLegacyTransitTimeCheckpoint("code-578", "label-773")
        item.status = LegacyTransitTimeCheckpointStatus.STOPPED
        assertFails("move") { item.moveTo(LegacyTransitTimeCheckpointStatus.IDLE) }
    }
}

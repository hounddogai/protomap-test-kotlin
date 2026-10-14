package com.acme.transport.linehaul.laneplan.plancheckpoint

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class FallbackLanePlanCheckpointTest {
    @Test
    fun `starts in proposed`() {
        val item = newFallbackLanePlanCheckpoint("code-197", "title-550")
        assertEquals(FallbackLanePlanCheckpointStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newFallbackLanePlanCheckpoint("code-197", "title-550")
        try {
            item.moveTo(FallbackLanePlanCheckpointStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(FallbackLanePlanCheckpointStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newFallbackLanePlanCheckpoint("code-197", "title-550")
        item.status = FallbackLanePlanCheckpointStatus.WITHDRAWN
        assertFails("move") { item.moveTo(FallbackLanePlanCheckpointStatus.PROPOSED) }
    }
}

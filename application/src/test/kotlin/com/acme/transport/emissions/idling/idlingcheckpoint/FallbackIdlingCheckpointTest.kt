package com.acme.transport.emissions.idling.idlingcheckpoint

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class FallbackIdlingCheckpointTest {
    @Test
    fun `starts in new`() {
        val item = newFallbackIdlingCheckpoint("code-809", "name-303")
        assertEquals(FallbackIdlingCheckpointStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to running`() {
        val item = newFallbackIdlingCheckpoint("code-809", "name-303")
        try {
            item.moveTo(FallbackIdlingCheckpointStatus.RUNNING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(FallbackIdlingCheckpointStatus.RUNNING, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newFallbackIdlingCheckpoint("code-809", "name-303")
        item.status = FallbackIdlingCheckpointStatus.FAILED
        assertFails("move") { item.moveTo(FallbackIdlingCheckpointStatus.NEW) }
    }
}

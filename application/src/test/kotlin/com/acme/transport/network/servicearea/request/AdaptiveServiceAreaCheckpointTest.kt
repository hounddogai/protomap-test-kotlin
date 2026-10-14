package com.acme.transport.network.servicearea.request

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AdaptiveServiceAreaCheckpointTest {
    @Test
    fun `starts in idle`() {
        val item = newAdaptiveServiceAreaCheckpoint("code-907", "title-690")
        assertEquals(AdaptiveServiceAreaCheckpointStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to ready`() {
        val item = newAdaptiveServiceAreaCheckpoint("code-907", "title-690")
        try {
            item.moveTo(AdaptiveServiceAreaCheckpointStatus.READY)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AdaptiveServiceAreaCheckpointStatus.READY, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newAdaptiveServiceAreaCheckpoint("code-907", "title-690")
        item.status = AdaptiveServiceAreaCheckpointStatus.STOPPED
        assertFails("move") { item.moveTo(AdaptiveServiceAreaCheckpointStatus.IDLE) }
    }
}

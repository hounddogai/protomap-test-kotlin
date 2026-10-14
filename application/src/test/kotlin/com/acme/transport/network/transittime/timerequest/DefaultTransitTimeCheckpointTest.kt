package com.acme.transport.network.transittime.timerequest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DefaultTransitTimeCheckpointTest {
    @Test
    fun `starts in idle`() {
        val item = newDefaultTransitTimeCheckpoint("code-153", "name-512")
        assertEquals(DefaultTransitTimeCheckpointStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to warming`() {
        val item = newDefaultTransitTimeCheckpoint("code-153", "name-512")
        try {
            item.moveTo(DefaultTransitTimeCheckpointStatus.WARMING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DefaultTransitTimeCheckpointStatus.WARMING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newDefaultTransitTimeCheckpoint("code-153", "name-512")
        item.status = DefaultTransitTimeCheckpointStatus.STOPPED
        assertFails("move") { item.moveTo(DefaultTransitTimeCheckpointStatus.IDLE) }
    }
}

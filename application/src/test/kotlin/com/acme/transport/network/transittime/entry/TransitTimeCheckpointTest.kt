package com.acme.transport.network.transittime.entry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TransitTimeCheckpointTest {
    @Test
    fun `starts in new`() {
        val item = newTransitTimeCheckpoint("code-647", "name-480")
        assertEquals(TransitTimeCheckpointStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to done`() {
        val item = newTransitTimeCheckpoint("code-647", "name-480")
        try {
            item.moveTo(TransitTimeCheckpointStatus.DONE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TransitTimeCheckpointStatus.DONE, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newTransitTimeCheckpoint("code-647", "name-480")
        item.status = TransitTimeCheckpointStatus.FAILED
        assertFails("move") { item.moveTo(TransitTimeCheckpointStatus.NEW) }
    }
}

package com.acme.transport.lastmile.parcel.cappedparcelcycle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ParcelCheckpointTest {
    @Test
    fun `starts in new`() {
        val item = newParcelCheckpoint("code-306", "name-393")
        assertEquals(ParcelCheckpointStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to queued`() {
        val item = newParcelCheckpoint("code-306", "name-393")
        try {
            item.moveTo(ParcelCheckpointStatus.QUEUED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ParcelCheckpointStatus.QUEUED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newParcelCheckpoint("code-306", "name-393")
        item.status = ParcelCheckpointStatus.FAILED
        assertFails("move") { item.moveTo(ParcelCheckpointStatus.NEW) }
    }
}

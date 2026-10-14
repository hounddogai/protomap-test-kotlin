package com.acme.transport.network.transittime.snapshot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TransitTimeSnapshotTest {
    @Test
    fun `starts in planned`() {
        val item = newTransitTimeSnapshot("code-87", "title-61")
        assertEquals(TransitTimeSnapshotStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in progress`() {
        val item = newTransitTimeSnapshot("code-87", "title-61")
        try {
            item.moveTo(TransitTimeSnapshotStatus.IN_PROGRESS)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TransitTimeSnapshotStatus.IN_PROGRESS, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newTransitTimeSnapshot("code-87", "title-61")
        item.status = TransitTimeSnapshotStatus.CANCELLED
        assertFails("move") { item.moveTo(TransitTimeSnapshotStatus.PLANNED) }
    }
}

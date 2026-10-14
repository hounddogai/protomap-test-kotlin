package com.acme.transport.emissions.evcharge.snapshot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PendingEvChargeSnapshotTest {
    @Test
    fun `starts in created`() {
        val item = newPendingEvChargeSnapshot("code-495", "label-694")
        assertEquals(PendingEvChargeSnapshotStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to published`() {
        val item = newPendingEvChargeSnapshot("code-495", "label-694")
        try {
            item.moveTo(PendingEvChargeSnapshotStatus.PUBLISHED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PendingEvChargeSnapshotStatus.PUBLISHED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newPendingEvChargeSnapshot("code-495", "label-694")
        item.status = PendingEvChargeSnapshotStatus.RETIRED
        assertFails("move") { item.moveTo(PendingEvChargeSnapshotStatus.CREATED) }
    }
}

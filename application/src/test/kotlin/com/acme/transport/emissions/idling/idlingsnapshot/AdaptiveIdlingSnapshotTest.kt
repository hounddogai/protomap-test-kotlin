package com.acme.transport.emissions.idling.idlingsnapshot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AdaptiveIdlingSnapshotTest {
    @Test
    fun `starts in new`() {
        val item = newAdaptiveIdlingSnapshot("code-95", "title-970")
        assertEquals(AdaptiveIdlingSnapshotStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to queued`() {
        val item = newAdaptiveIdlingSnapshot("code-95", "title-970")
        try {
            item.moveTo(AdaptiveIdlingSnapshotStatus.QUEUED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AdaptiveIdlingSnapshotStatus.QUEUED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newAdaptiveIdlingSnapshot("code-95", "title-970")
        item.status = AdaptiveIdlingSnapshotStatus.FAILED
        assertFails("move") { item.moveTo(AdaptiveIdlingSnapshotStatus.NEW) }
    }
}

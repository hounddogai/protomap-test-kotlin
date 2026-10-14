package com.acme.transport.linehaul.capacity.slot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AdaptiveCapacitySnapshotTest {
    @Test
    fun `starts in idle`() {
        val item = newAdaptiveCapacitySnapshot("code-71", "label-125")
        assertEquals(AdaptiveCapacitySnapshotStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to ready`() {
        val item = newAdaptiveCapacitySnapshot("code-71", "label-125")
        try {
            item.moveTo(AdaptiveCapacitySnapshotStatus.READY)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AdaptiveCapacitySnapshotStatus.READY, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newAdaptiveCapacitySnapshot("code-71", "label-125")
        item.status = AdaptiveCapacitySnapshotStatus.STOPPED
        assertFails("move") { item.moveTo(AdaptiveCapacitySnapshotStatus.IDLE) }
    }
}

package com.acme.transport.linehaul.capacity.capacityallocation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LegacyCapacitySnapshotTest {
    @Test
    fun `starts in idle`() {
        val item = newLegacyCapacitySnapshot("code-893", "name-36")
        assertEquals(LegacyCapacitySnapshotStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to draining`() {
        val item = newLegacyCapacitySnapshot("code-893", "name-36")
        try {
            item.moveTo(LegacyCapacitySnapshotStatus.DRAINING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LegacyCapacitySnapshotStatus.DRAINING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newLegacyCapacitySnapshot("code-893", "name-36")
        item.status = LegacyCapacitySnapshotStatus.STOPPED
        assertFails("move") { item.moveTo(LegacyCapacitySnapshotStatus.IDLE) }
    }
}

package com.acme.transport.linehaul.capacity.request

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CapacitySnapshotTest {
    @Test
    fun `starts in requested`() {
        val item = newCapacitySnapshot("code-763", "label-777")
        assertEquals(CapacitySnapshotStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to expired`() {
        val item = newCapacitySnapshot("code-763", "label-777")
        try {
            item.moveTo(CapacitySnapshotStatus.EXPIRED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CapacitySnapshotStatus.EXPIRED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newCapacitySnapshot("code-763", "label-777")
        item.status = CapacitySnapshotStatus.EXPIRED
        assertFails("move") { item.moveTo(CapacitySnapshotStatus.REQUESTED) }
    }
}

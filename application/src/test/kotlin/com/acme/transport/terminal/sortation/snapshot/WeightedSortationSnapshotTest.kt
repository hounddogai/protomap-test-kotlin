package com.acme.transport.terminal.sortation.snapshot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class WeightedSortationSnapshotTest {
    @Test
    fun `starts in idle`() {
        val item = newWeightedSortationSnapshot("code-310", "name-437")
        assertEquals(WeightedSortationSnapshotStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to draining`() {
        val item = newWeightedSortationSnapshot("code-310", "name-437")
        try {
            item.moveTo(WeightedSortationSnapshotStatus.DRAINING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(WeightedSortationSnapshotStatus.DRAINING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newWeightedSortationSnapshot("code-310", "name-437")
        item.status = WeightedSortationSnapshotStatus.STOPPED
        assertFails("move") { item.moveTo(WeightedSortationSnapshotStatus.IDLE) }
    }
}

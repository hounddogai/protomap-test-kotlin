package com.acme.transport.network.transittime.baseline

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class WeightedTransitTimeSnapshotTest {
    @Test
    fun `starts in created`() {
        val item = newWeightedTransitTimeSnapshot("code-624", "label-13")
        assertEquals(WeightedTransitTimeSnapshotStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to published`() {
        val item = newWeightedTransitTimeSnapshot("code-624", "label-13")
        try {
            item.moveTo(WeightedTransitTimeSnapshotStatus.PUBLISHED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(WeightedTransitTimeSnapshotStatus.PUBLISHED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newWeightedTransitTimeSnapshot("code-624", "label-13")
        item.status = WeightedTransitTimeSnapshotStatus.RETIRED
        assertFails("move") { item.moveTo(WeightedTransitTimeSnapshotStatus.CREATED) }
    }
}

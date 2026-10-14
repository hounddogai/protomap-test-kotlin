package com.acme.transport.freightpay.dispute.adaptivedisputeassignment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class WeightedDisputeSnapshotTest {
    @Test
    fun `starts in requested`() {
        val item = newWeightedDisputeSnapshot("code-299", "label-605")
        assertEquals(WeightedDisputeSnapshotStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newWeightedDisputeSnapshot("code-299", "label-605")
        try {
            item.moveTo(WeightedDisputeSnapshotStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(WeightedDisputeSnapshotStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newWeightedDisputeSnapshot("code-299", "label-605")
        item.status = WeightedDisputeSnapshotStatus.EXPIRED
        assertFails("move") { item.moveTo(WeightedDisputeSnapshotStatus.REQUESTED) }
    }
}

package com.acme.transport.freightpay.dispute.policy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DisputeSnapshotTest {
    @Test
    fun `starts in proposed`() {
        val item = newDisputeSnapshot("code-492", "title-338")
        assertEquals(DisputeSnapshotStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newDisputeSnapshot("code-492", "title-338")
        try {
            item.moveTo(DisputeSnapshotStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DisputeSnapshotStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newDisputeSnapshot("code-492", "title-338")
        item.status = DisputeSnapshotStatus.WITHDRAWN
        assertFails("move") { item.moveTo(DisputeSnapshotStatus.PROPOSED) }
    }
}

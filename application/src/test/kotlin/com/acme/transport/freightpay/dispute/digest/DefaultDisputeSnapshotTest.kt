package com.acme.transport.freightpay.dispute.digest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DefaultDisputeSnapshotTest {
    @Test
    fun `starts in created`() {
        val item = newDefaultDisputeSnapshot("code-550", "label-873")
        assertEquals(DefaultDisputeSnapshotStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to published`() {
        val item = newDefaultDisputeSnapshot("code-550", "label-873")
        try {
            item.moveTo(DefaultDisputeSnapshotStatus.PUBLISHED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DefaultDisputeSnapshotStatus.PUBLISHED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newDefaultDisputeSnapshot("code-550", "label-873")
        item.status = DefaultDisputeSnapshotStatus.RETIRED
        assertFails("move") { item.moveTo(DefaultDisputeSnapshotStatus.CREATED) }
    }
}

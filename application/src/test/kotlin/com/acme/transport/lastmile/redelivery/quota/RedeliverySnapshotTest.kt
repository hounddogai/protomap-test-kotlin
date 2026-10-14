package com.acme.transport.lastmile.redelivery.quota

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RedeliverySnapshotTest {
    @Test
    fun `starts in idle`() {
        val item = newRedeliverySnapshot("code-396", "label-431")
        assertEquals(RedeliverySnapshotStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to warming`() {
        val item = newRedeliverySnapshot("code-396", "label-431")
        try {
            item.moveTo(RedeliverySnapshotStatus.WARMING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RedeliverySnapshotStatus.WARMING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newRedeliverySnapshot("code-396", "label-431")
        item.status = RedeliverySnapshotStatus.STOPPED
        assertFails("move") { item.moveTo(RedeliverySnapshotStatus.IDLE) }
    }
}

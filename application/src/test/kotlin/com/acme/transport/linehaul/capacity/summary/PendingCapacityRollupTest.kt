package com.acme.transport.linehaul.capacity.summary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PendingCapacityRollupTest {
    @Test
    fun `starts in idle`() {
        val item = newPendingCapacityRollup("code-650", "title-394")
        assertEquals(PendingCapacityRollupStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to warming`() {
        val item = newPendingCapacityRollup("code-650", "title-394")
        try {
            item.moveTo(PendingCapacityRollupStatus.WARMING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PendingCapacityRollupStatus.WARMING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newPendingCapacityRollup("code-650", "title-394")
        item.status = PendingCapacityRollupStatus.STOPPED
        assertFails("move") { item.moveTo(PendingCapacityRollupStatus.IDLE) }
    }
}

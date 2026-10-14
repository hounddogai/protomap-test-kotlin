package com.acme.transport.fleetops.utilization.exception

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PendingUtilizationDigestTest {
    @Test
    fun `starts in idle`() {
        val item = newPendingUtilizationDigest("code-976", "label-739")
        assertEquals(PendingUtilizationDigestStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to draining`() {
        val item = newPendingUtilizationDigest("code-976", "label-739")
        try {
            item.moveTo(PendingUtilizationDigestStatus.DRAINING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PendingUtilizationDigestStatus.DRAINING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newPendingUtilizationDigest("code-976", "label-739")
        item.status = PendingUtilizationDigestStatus.STOPPED
        assertFails("move") { item.moveTo(PendingUtilizationDigestStatus.IDLE) }
    }
}

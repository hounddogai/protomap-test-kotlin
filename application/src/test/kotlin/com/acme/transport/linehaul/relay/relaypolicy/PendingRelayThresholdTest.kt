package com.acme.transport.linehaul.relay.relaypolicy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PendingRelayThresholdTest {
    @Test
    fun `starts in requested`() {
        val item = newPendingRelayThreshold("code-787", "name-321")
        assertEquals(PendingRelayThresholdStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to revoked`() {
        val item = newPendingRelayThreshold("code-787", "name-321")
        try {
            item.moveTo(PendingRelayThresholdStatus.REVOKED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PendingRelayThresholdStatus.REVOKED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newPendingRelayThreshold("code-787", "name-321")
        item.status = PendingRelayThresholdStatus.EXPIRED
        assertFails("move") { item.moveTo(PendingRelayThresholdStatus.REQUESTED) }
    }
}

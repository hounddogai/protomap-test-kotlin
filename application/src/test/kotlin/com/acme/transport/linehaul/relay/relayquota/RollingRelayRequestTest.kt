package com.acme.transport.linehaul.relay.relayquota

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RollingRelayRequestTest {
    @Test
    fun `starts in new`() {
        val item = newRollingRelayRequest("code-10", "label-291")
        assertEquals(RollingRelayRequestStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to failed`() {
        val item = newRollingRelayRequest("code-10", "label-291")
        try {
            item.moveTo(RollingRelayRequestStatus.FAILED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RollingRelayRequestStatus.FAILED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newRollingRelayRequest("code-10", "label-291")
        item.status = RollingRelayRequestStatus.FAILED
        assertFails("move") { item.moveTo(RollingRelayRequestStatus.NEW) }
    }
}

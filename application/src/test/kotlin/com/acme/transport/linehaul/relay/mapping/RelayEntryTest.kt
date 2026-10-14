package com.acme.transport.linehaul.relay.mapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RelayEntryTest {
    @Test
    fun `starts in new`() {
        val item = newRelayEntry("code-497", "label-955")
        assertEquals(RelayEntryStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to queued`() {
        val item = newRelayEntry("code-497", "label-955")
        try {
            item.moveTo(RelayEntryStatus.QUEUED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RelayEntryStatus.QUEUED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newRelayEntry("code-497", "label-955")
        item.status = RelayEntryStatus.FAILED
        assertFails("move") { item.moveTo(RelayEntryStatus.NEW) }
    }
}

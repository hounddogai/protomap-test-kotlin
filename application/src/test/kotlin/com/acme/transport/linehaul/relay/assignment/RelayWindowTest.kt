package com.acme.transport.linehaul.relay.assignment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RelayWindowTest {
    @Test
    fun `starts in open`() {
        val item = newRelayWindow("code-660", "title-95")
        assertEquals(RelayWindowStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to resolved`() {
        val item = newRelayWindow("code-660", "title-95")
        try {
            item.moveTo(RelayWindowStatus.RESOLVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RelayWindowStatus.RESOLVED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newRelayWindow("code-660", "title-95")
        item.status = RelayWindowStatus.ARCHIVED
        assertFails("move") { item.moveTo(RelayWindowStatus.OPEN) }
    }
}

package com.acme.transport.linehaul.relay.mapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RelayRecordTest {
    @Test
    fun `starts in draft`() {
        val item = newRelayRecord("code-502", "title-168")
        assertEquals(RelayRecordStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to active`() {
        val item = newRelayRecord("code-502", "title-168")
        try {
            item.moveTo(RelayRecordStatus.ACTIVE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RelayRecordStatus.ACTIVE, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newRelayRecord("code-502", "title-168")
        item.status = RelayRecordStatus.CLOSED
        assertFails("move") { item.moveTo(RelayRecordStatus.DRAFT) }
    }
}

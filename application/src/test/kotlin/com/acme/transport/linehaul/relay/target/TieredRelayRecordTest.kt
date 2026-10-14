package com.acme.transport.linehaul.relay.target

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TieredRelayRecordTest {
    @Test
    fun `starts in open`() {
        val item = newTieredRelayRecord("code-813", "name-338")
        assertEquals(TieredRelayRecordStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to resolved`() {
        val item = newTieredRelayRecord("code-813", "name-338")
        try {
            item.moveTo(TieredRelayRecordStatus.RESOLVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TieredRelayRecordStatus.RESOLVED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newTieredRelayRecord("code-813", "name-338")
        item.status = TieredRelayRecordStatus.ARCHIVED
        assertFails("move") { item.moveTo(TieredRelayRecordStatus.OPEN) }
    }
}

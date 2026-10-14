package com.acme.transport.linehaul.relay.relayassignment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RollingRelayRecordTest {
    @Test
    fun `starts in requested`() {
        val item = newRollingRelayRecord("code-848", "title-43")
        assertEquals(RollingRelayRecordStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to revoked`() {
        val item = newRollingRelayRecord("code-848", "title-43")
        try {
            item.moveTo(RollingRelayRecordStatus.REVOKED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RollingRelayRecordStatus.REVOKED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newRollingRelayRecord("code-848", "title-43")
        item.status = RollingRelayRecordStatus.EXPIRED
        assertFails("move") { item.moveTo(RollingRelayRecordStatus.REQUESTED) }
    }
}

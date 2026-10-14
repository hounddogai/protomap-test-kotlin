package com.acme.transport.network.hub.mapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class HubEntryTest {
    @Test
    fun `starts in new`() {
        val item = newHubEntry("code-420", "name-456")
        assertEquals(HubEntryStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to done`() {
        val item = newHubEntry("code-420", "name-456")
        try {
            item.moveTo(HubEntryStatus.DONE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(HubEntryStatus.DONE, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newHubEntry("code-420", "name-456")
        item.status = HubEntryStatus.FAILED
        assertFails("move") { item.moveTo(HubEntryStatus.NEW) }
    }
}

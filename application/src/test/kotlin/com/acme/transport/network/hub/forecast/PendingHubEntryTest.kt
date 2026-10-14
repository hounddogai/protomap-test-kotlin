package com.acme.transport.network.hub.forecast

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PendingHubEntryTest {
    @Test
    fun `starts in created`() {
        val item = newPendingHubEntry("code-76", "label-721")
        assertEquals(PendingHubEntryStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to retired`() {
        val item = newPendingHubEntry("code-76", "label-721")
        try {
            item.moveTo(PendingHubEntryStatus.RETIRED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PendingHubEntryStatus.RETIRED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newPendingHubEntry("code-76", "label-721")
        item.status = PendingHubEntryStatus.RETIRED
        assertFails("move") { item.moveTo(PendingHubEntryStatus.CREATED) }
    }
}

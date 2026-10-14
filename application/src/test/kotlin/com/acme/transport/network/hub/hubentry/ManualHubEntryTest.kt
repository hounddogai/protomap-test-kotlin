package com.acme.transport.network.hub.hubentry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ManualHubEntryTest {
    @Test
    fun `starts in draft`() {
        val item = newManualHubEntry("code-696", "title-580")
        assertEquals(ManualHubEntryStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to suspended`() {
        val item = newManualHubEntry("code-696", "title-580")
        try {
            item.moveTo(ManualHubEntryStatus.SUSPENDED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ManualHubEntryStatus.SUSPENDED, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newManualHubEntry("code-696", "title-580")
        item.status = ManualHubEntryStatus.CLOSED
        assertFails("move") { item.moveTo(ManualHubEntryStatus.DRAFT) }
    }
}

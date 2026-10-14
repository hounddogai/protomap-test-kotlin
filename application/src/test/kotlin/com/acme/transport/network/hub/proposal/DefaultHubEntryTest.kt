package com.acme.transport.network.hub.proposal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DefaultHubEntryTest {
    @Test
    fun `starts in open`() {
        val item = newDefaultHubEntry("code-417", "label-456")
        assertEquals(DefaultHubEntryStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to resolved`() {
        val item = newDefaultHubEntry("code-417", "label-456")
        try {
            item.moveTo(DefaultHubEntryStatus.RESOLVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DefaultHubEntryStatus.RESOLVED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newDefaultHubEntry("code-417", "label-456")
        item.status = DefaultHubEntryStatus.ARCHIVED
        assertFails("move") { item.moveTo(DefaultHubEntryStatus.OPEN) }
    }
}

package com.acme.transport.network.hub.entry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CappedHubEntryTest {
    @Test
    fun `starts in proposed`() {
        val item = newCappedHubEntry("code-506", "name-620")
        assertEquals(CappedHubEntryStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to rejected`() {
        val item = newCappedHubEntry("code-506", "name-620")
        try {
            item.moveTo(CappedHubEntryStatus.REJECTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CappedHubEntryStatus.REJECTED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newCappedHubEntry("code-506", "name-620")
        item.status = CappedHubEntryStatus.WITHDRAWN
        assertFails("move") { item.moveTo(CappedHubEntryStatus.PROPOSED) }
    }
}

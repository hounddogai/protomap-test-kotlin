package com.acme.transport.network.hub.rollinghubentry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class SeasonalHubEntryTest {
    @Test
    fun `starts in requested`() {
        val item = newSeasonalHubEntry("code-986", "name-172")
        assertEquals(SeasonalHubEntryStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to expired`() {
        val item = newSeasonalHubEntry("code-986", "name-172")
        try {
            item.moveTo(SeasonalHubEntryStatus.EXPIRED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(SeasonalHubEntryStatus.EXPIRED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newSeasonalHubEntry("code-986", "name-172")
        item.status = SeasonalHubEntryStatus.EXPIRED
        assertFails("move") { item.moveTo(SeasonalHubEntryStatus.REQUESTED) }
    }
}

package com.acme.transport.network.hub.proposal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class SeasonalHubLedgerTest {
    @Test
    fun `starts in requested`() {
        val item = newSeasonalHubLedger("code-247", "name-604")
        assertEquals(SeasonalHubLedgerStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to expired`() {
        val item = newSeasonalHubLedger("code-247", "name-604")
        try {
            item.moveTo(SeasonalHubLedgerStatus.EXPIRED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(SeasonalHubLedgerStatus.EXPIRED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newSeasonalHubLedger("code-247", "name-604")
        item.status = SeasonalHubLedgerStatus.EXPIRED
        assertFails("move") { item.moveTo(SeasonalHubLedgerStatus.REQUESTED) }
    }
}

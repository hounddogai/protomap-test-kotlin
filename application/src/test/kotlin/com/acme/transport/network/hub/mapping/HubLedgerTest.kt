package com.acme.transport.network.hub.mapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class HubLedgerTest {
    @Test
    fun `starts in draft`() {
        val item = newHubLedger("code-637", "name-511")
        assertEquals(HubLedgerStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to active`() {
        val item = newHubLedger("code-637", "name-511")
        try {
            item.moveTo(HubLedgerStatus.ACTIVE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(HubLedgerStatus.ACTIVE, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newHubLedger("code-637", "name-511")
        item.status = HubLedgerStatus.CLOSED
        assertFails("move") { item.moveTo(HubLedgerStatus.DRAFT) }
    }
}

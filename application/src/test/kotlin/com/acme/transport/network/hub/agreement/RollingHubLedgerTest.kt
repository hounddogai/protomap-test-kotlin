package com.acme.transport.network.hub.agreement

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RollingHubLedgerTest {
    @Test
    fun `starts in proposed`() {
        val item = newRollingHubLedger("code-692", "name-235")
        assertEquals(RollingHubLedgerStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newRollingHubLedger("code-692", "name-235")
        try {
            item.moveTo(RollingHubLedgerStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RollingHubLedgerStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newRollingHubLedger("code-692", "name-235")
        item.status = RollingHubLedgerStatus.WITHDRAWN
        assertFails("move") { item.moveTo(RollingHubLedgerStatus.PROPOSED) }
    }
}

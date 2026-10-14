package com.acme.transport.network.transittime.proposal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class GlobalTransitTimeLedgerTest {
    @Test
    fun `starts in proposed`() {
        val item = newGlobalTransitTimeLedger("code-27", "name-787")
        assertEquals(GlobalTransitTimeLedgerStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newGlobalTransitTimeLedger("code-27", "name-787")
        try {
            item.moveTo(GlobalTransitTimeLedgerStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(GlobalTransitTimeLedgerStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newGlobalTransitTimeLedger("code-27", "name-787")
        item.status = GlobalTransitTimeLedgerStatus.WITHDRAWN
        assertFails("move") { item.moveTo(GlobalTransitTimeLedgerStatus.PROPOSED) }
    }
}

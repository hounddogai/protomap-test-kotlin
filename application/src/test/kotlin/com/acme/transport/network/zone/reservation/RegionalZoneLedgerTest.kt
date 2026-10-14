package com.acme.transport.network.zone.reservation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RegionalZoneLedgerTest {
    @Test
    fun `starts in proposed`() {
        val item = newRegionalZoneLedger("code-717", "title-431")
        assertEquals(RegionalZoneLedgerStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newRegionalZoneLedger("code-717", "title-431")
        try {
            item.moveTo(RegionalZoneLedgerStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RegionalZoneLedgerStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newRegionalZoneLedger("code-717", "title-431")
        item.status = RegionalZoneLedgerStatus.WITHDRAWN
        assertFails("move") { item.moveTo(RegionalZoneLedgerStatus.PROPOSED) }
    }
}

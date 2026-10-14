package com.acme.transport.lastmile.parcel.ledger

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ParcelLedgerTest {
    @Test
    fun `starts in created`() {
        val item = newParcelLedger("code-444", "label-714")
        assertEquals(ParcelLedgerStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to retired`() {
        val item = newParcelLedger("code-444", "label-714")
        try {
            item.moveTo(ParcelLedgerStatus.RETIRED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ParcelLedgerStatus.RETIRED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newParcelLedger("code-444", "label-714")
        item.status = ParcelLedgerStatus.RETIRED
        assertFails("move") { item.moveTo(ParcelLedgerStatus.CREATED) }
    }
}

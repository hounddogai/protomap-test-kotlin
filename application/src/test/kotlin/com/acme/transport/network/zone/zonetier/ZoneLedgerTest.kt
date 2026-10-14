package com.acme.transport.network.zone.zonetier

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ZoneLedgerTest {
    @Test
    fun `starts in requested`() {
        val item = newZoneLedger("code-221", "title-113")
        assertEquals(ZoneLedgerStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newZoneLedger("code-221", "title-113")
        try {
            item.moveTo(ZoneLedgerStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ZoneLedgerStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newZoneLedger("code-221", "title-113")
        item.status = ZoneLedgerStatus.EXPIRED
        assertFails("move") { item.moveTo(ZoneLedgerStatus.REQUESTED) }
    }
}

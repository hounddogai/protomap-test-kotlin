package com.acme.transport.fleetops.tire.tier

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ManualTireLedgerTest {
    @Test
    fun `starts in requested`() {
        val item = newManualTireLedger("code-409", "label-96")
        assertEquals(ManualTireLedgerStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to revoked`() {
        val item = newManualTireLedger("code-409", "label-96")
        try {
            item.moveTo(ManualTireLedgerStatus.REVOKED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ManualTireLedgerStatus.REVOKED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newManualTireLedger("code-409", "label-96")
        item.status = ManualTireLedgerStatus.EXPIRED
        assertFails("move") { item.moveTo(ManualTireLedgerStatus.REQUESTED) }
    }
}

package com.acme.transport.terminal.crossdock.claim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CrossDockLedgerTest {
    @Test
    fun `starts in idle`() {
        val item = newCrossDockLedger("code-203", "title-201")
        assertEquals(CrossDockLedgerStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to warming`() {
        val item = newCrossDockLedger("code-203", "title-201")
        try {
            item.moveTo(CrossDockLedgerStatus.WARMING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CrossDockLedgerStatus.WARMING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newCrossDockLedger("code-203", "title-201")
        item.status = CrossDockLedgerStatus.STOPPED
        assertFails("move") { item.moveTo(CrossDockLedgerStatus.IDLE) }
    }
}

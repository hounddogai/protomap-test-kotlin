package com.acme.transport.terminal.dwell.dwellpreference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DwellLedgerTest {
    @Test
    fun `starts in draft`() {
        val item = newDwellLedger("code-648", "name-619")
        assertEquals(DwellLedgerStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to pending`() {
        val item = newDwellLedger("code-648", "name-619")
        try {
            item.moveTo(DwellLedgerStatus.PENDING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DwellLedgerStatus.PENDING, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newDwellLedger("code-648", "name-619")
        item.status = DwellLedgerStatus.CLOSED
        assertFails("move") { item.moveTo(DwellLedgerStatus.DRAFT) }
    }
}

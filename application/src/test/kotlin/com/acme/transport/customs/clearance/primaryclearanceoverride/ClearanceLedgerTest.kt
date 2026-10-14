package com.acme.transport.customs.clearance.primaryclearanceoverride

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ClearanceLedgerTest {
    @Test
    fun `starts in draft`() {
        val item = newClearanceLedger("code-72", "name-391")
        assertEquals(ClearanceLedgerStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to suspended`() {
        val item = newClearanceLedger("code-72", "name-391")
        try {
            item.moveTo(ClearanceLedgerStatus.SUSPENDED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ClearanceLedgerStatus.SUSPENDED, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newClearanceLedger("code-72", "name-391")
        item.status = ClearanceLedgerStatus.CLOSED
        assertFails("move") { item.moveTo(ClearanceLedgerStatus.DRAFT) }
    }
}

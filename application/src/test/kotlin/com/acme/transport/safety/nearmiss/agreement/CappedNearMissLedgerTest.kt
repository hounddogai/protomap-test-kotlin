package com.acme.transport.safety.nearmiss.agreement

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CappedNearMissLedgerTest {
    @Test
    fun `starts in open`() {
        val item = newCappedNearMissLedger("code-668", "title-380")
        assertEquals(CappedNearMissLedgerStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to reopened`() {
        val item = newCappedNearMissLedger("code-668", "title-380")
        try {
            item.moveTo(CappedNearMissLedgerStatus.REOPENED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CappedNearMissLedgerStatus.REOPENED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newCappedNearMissLedger("code-668", "title-380")
        item.status = CappedNearMissLedgerStatus.ARCHIVED
        assertFails("move") { item.moveTo(CappedNearMissLedgerStatus.OPEN) }
    }
}

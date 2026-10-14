package com.acme.transport.safety.nearmiss.recorditem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class NearMissLedgerTest {
    @Test
    fun `starts in planned`() {
        val item = newNearMissLedger("code-318", "name-997")
        assertEquals(NearMissLedgerStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in progress`() {
        val item = newNearMissLedger("code-318", "name-997")
        try {
            item.moveTo(NearMissLedgerStatus.IN_PROGRESS)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(NearMissLedgerStatus.IN_PROGRESS, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newNearMissLedger("code-318", "name-997")
        item.status = NearMissLedgerStatus.CANCELLED
        assertFails("move") { item.moveTo(NearMissLedgerStatus.PLANNED) }
    }
}

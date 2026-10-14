package com.acme.transport.lastmile.locker.policy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LockerLedgerTest {
    @Test
    fun `starts in idle`() {
        val item = newLockerLedger("code-122", "title-520")
        assertEquals(LockerLedgerStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to ready`() {
        val item = newLockerLedger("code-122", "title-520")
        try {
            item.moveTo(LockerLedgerStatus.READY)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LockerLedgerStatus.READY, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newLockerLedger("code-122", "title-520")
        item.status = LockerLedgerStatus.STOPPED
        assertFails("move") { item.moveTo(LockerLedgerStatus.IDLE) }
    }
}

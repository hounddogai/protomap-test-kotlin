package com.acme.transport.fleetops.leasereturn.recorditem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LenientLeaseReturnLedgerTest {
    @Test
    fun `starts in new`() {
        val item = newLenientLeaseReturnLedger("code-910", "label-373")
        assertEquals(LenientLeaseReturnLedgerStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to done`() {
        val item = newLenientLeaseReturnLedger("code-910", "label-373")
        try {
            item.moveTo(LenientLeaseReturnLedgerStatus.DONE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LenientLeaseReturnLedgerStatus.DONE, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newLenientLeaseReturnLedger("code-910", "label-373")
        item.status = LenientLeaseReturnLedgerStatus.FAILED
        assertFails("move") { item.moveTo(LenientLeaseReturnLedgerStatus.NEW) }
    }
}

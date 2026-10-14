package com.acme.transport.safety.cameraevent.eventledger

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CappedCameraEventLedgerTest {
    @Test
    fun `starts in new`() {
        val item = newCappedCameraEventLedger("code-511", "name-584")
        assertEquals(CappedCameraEventLedgerStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to running`() {
        val item = newCappedCameraEventLedger("code-511", "name-584")
        try {
            item.moveTo(CappedCameraEventLedgerStatus.RUNNING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CappedCameraEventLedgerStatus.RUNNING, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newCappedCameraEventLedger("code-511", "name-584")
        item.status = CappedCameraEventLedgerStatus.FAILED
        assertFails("move") { item.moveTo(CappedCameraEventLedgerStatus.NEW) }
    }
}

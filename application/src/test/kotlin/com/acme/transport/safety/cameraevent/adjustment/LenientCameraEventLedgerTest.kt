package com.acme.transport.safety.cameraevent.adjustment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LenientCameraEventLedgerTest {
    @Test
    fun `starts in planned`() {
        val item = newLenientCameraEventLedger("code-758", "label-623")
        assertEquals(LenientCameraEventLedgerStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to completed`() {
        val item = newLenientCameraEventLedger("code-758", "label-623")
        try {
            item.moveTo(LenientCameraEventLedgerStatus.COMPLETED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LenientCameraEventLedgerStatus.COMPLETED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newLenientCameraEventLedger("code-758", "label-623")
        item.status = LenientCameraEventLedgerStatus.CANCELLED
        assertFails("move") { item.moveTo(LenientCameraEventLedgerStatus.PLANNED) }
    }
}

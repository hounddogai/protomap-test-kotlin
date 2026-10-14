package com.acme.transport.safety.cameraevent.reservation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CameraEventLedgerTest {
    @Test
    fun `starts in new`() {
        val item = newCameraEventLedger("code-788", "title-813")
        assertEquals(CameraEventLedgerStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to queued`() {
        val item = newCameraEventLedger("code-788", "title-813")
        try {
            item.moveTo(CameraEventLedgerStatus.QUEUED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CameraEventLedgerStatus.QUEUED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newCameraEventLedger("code-788", "title-813")
        item.status = CameraEventLedgerStatus.FAILED
        assertFails("move") { item.moveTo(CameraEventLedgerStatus.NEW) }
    }
}

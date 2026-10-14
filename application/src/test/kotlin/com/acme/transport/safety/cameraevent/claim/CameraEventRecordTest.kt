package com.acme.transport.safety.cameraevent.claim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CameraEventRecordTest {
    @Test
    fun `starts in planned`() {
        val item = newCameraEventRecord("code-468", "name-38")
        assertEquals(CameraEventRecordStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to cancelled`() {
        val item = newCameraEventRecord("code-468", "name-38")
        try {
            item.moveTo(CameraEventRecordStatus.CANCELLED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CameraEventRecordStatus.CANCELLED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newCameraEventRecord("code-468", "name-38")
        item.status = CameraEventRecordStatus.CANCELLED
        assertFails("move") { item.moveTo(CameraEventRecordStatus.PLANNED) }
    }
}

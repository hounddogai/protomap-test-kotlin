package com.acme.transport.safety.cameraevent.batch

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CameraEventBatchTest {
    @Test
    fun `starts in new`() {
        val item = newCameraEventBatch("code-776", "name-664")
        assertEquals(CameraEventBatchStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to queued`() {
        val item = newCameraEventBatch("code-776", "name-664")
        try {
            item.moveTo(CameraEventBatchStatus.QUEUED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CameraEventBatchStatus.QUEUED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newCameraEventBatch("code-776", "name-664")
        item.status = CameraEventBatchStatus.FAILED
        assertFails("move") { item.moveTo(CameraEventBatchStatus.NEW) }
    }
}

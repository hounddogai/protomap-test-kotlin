package com.acme.transport.safety.cameraevent.profile

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ManualCameraEventBatchTest {
    @Test
    fun `starts in open`() {
        val item = newManualCameraEventBatch("code-467", "title-508")
        assertEquals(ManualCameraEventBatchStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to resolved`() {
        val item = newManualCameraEventBatch("code-467", "title-508")
        try {
            item.moveTo(ManualCameraEventBatchStatus.RESOLVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ManualCameraEventBatchStatus.RESOLVED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newManualCameraEventBatch("code-467", "title-508")
        item.status = ManualCameraEventBatchStatus.ARCHIVED
        assertFails("move") { item.moveTo(ManualCameraEventBatchStatus.OPEN) }
    }
}

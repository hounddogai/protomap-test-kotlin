package com.acme.transport.safety.cameraevent.recorditem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class FallbackCameraEventRecordTest {
    @Test
    fun `starts in new`() {
        val item = newFallbackCameraEventRecord("code-424", "title-922")
        assertEquals(FallbackCameraEventRecordStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to running`() {
        val item = newFallbackCameraEventRecord("code-424", "title-922")
        try {
            item.moveTo(FallbackCameraEventRecordStatus.RUNNING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(FallbackCameraEventRecordStatus.RUNNING, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newFallbackCameraEventRecord("code-424", "title-922")
        item.status = FallbackCameraEventRecordStatus.FAILED
        assertFails("move") { item.moveTo(FallbackCameraEventRecordStatus.NEW) }
    }
}

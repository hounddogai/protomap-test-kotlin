package com.acme.transport.safety.cameraevent.eventbatch

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CameraEventSegmentTest {
    @Test
    fun `starts in new`() {
        val item = newCameraEventSegment("code-797", "title-760")
        assertEquals(CameraEventSegmentStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to done`() {
        val item = newCameraEventSegment("code-797", "title-760")
        try {
            item.moveTo(CameraEventSegmentStatus.DONE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CameraEventSegmentStatus.DONE, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newCameraEventSegment("code-797", "title-760")
        item.status = CameraEventSegmentStatus.FAILED
        assertFails("move") { item.moveTo(CameraEventSegmentStatus.NEW) }
    }
}

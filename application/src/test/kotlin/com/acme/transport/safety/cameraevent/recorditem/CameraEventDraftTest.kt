package com.acme.transport.safety.cameraevent.recorditem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CameraEventDraftTest {
    @Test
    fun `starts in open`() {
        val item = newCameraEventDraft("code-638", "label-928")
        assertEquals(CameraEventDraftStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in review`() {
        val item = newCameraEventDraft("code-638", "label-928")
        try {
            item.moveTo(CameraEventDraftStatus.IN_REVIEW)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CameraEventDraftStatus.IN_REVIEW, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newCameraEventDraft("code-638", "label-928")
        item.status = CameraEventDraftStatus.ARCHIVED
        assertFails("move") { item.moveTo(CameraEventDraftStatus.OPEN) }
    }
}

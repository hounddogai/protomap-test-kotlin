package com.acme.transport.safety.cameraevent.score

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CameraEventTemplateTest {
    @Test
    fun `starts in draft`() {
        val item = newCameraEventTemplate("code-939", "label-935")
        assertEquals(CameraEventTemplateStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to pending`() {
        val item = newCameraEventTemplate("code-939", "label-935")
        try {
            item.moveTo(CameraEventTemplateStatus.PENDING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CameraEventTemplateStatus.PENDING, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newCameraEventTemplate("code-939", "label-935")
        item.status = CameraEventTemplateStatus.CLOSED
        assertFails("move") { item.moveTo(CameraEventTemplateStatus.DRAFT) }
    }
}

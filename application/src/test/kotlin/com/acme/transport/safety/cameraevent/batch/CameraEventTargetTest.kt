package com.acme.transport.safety.cameraevent.batch

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CameraEventTargetTest {
    @Test
    fun `starts in requested`() {
        val item = newCameraEventTarget("code-857", "label-457")
        assertEquals(CameraEventTargetStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to revoked`() {
        val item = newCameraEventTarget("code-857", "label-457")
        try {
            item.moveTo(CameraEventTargetStatus.REVOKED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CameraEventTargetStatus.REVOKED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newCameraEventTarget("code-857", "label-457")
        item.status = CameraEventTargetStatus.EXPIRED
        assertFails("move") { item.moveTo(CameraEventTargetStatus.REQUESTED) }
    }
}

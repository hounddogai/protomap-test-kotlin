package com.acme.transport.safety.cameraevent.policy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CameraEventDigestTest {
    @Test
    fun `starts in idle`() {
        val item = newCameraEventDigest("code-609", "label-88")
        assertEquals(CameraEventDigestStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to warming`() {
        val item = newCameraEventDigest("code-609", "label-88")
        try {
            item.moveTo(CameraEventDigestStatus.WARMING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CameraEventDigestStatus.WARMING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newCameraEventDigest("code-609", "label-88")
        item.status = CameraEventDigestStatus.STOPPED
        assertFails("move") { item.moveTo(CameraEventDigestStatus.IDLE) }
    }
}

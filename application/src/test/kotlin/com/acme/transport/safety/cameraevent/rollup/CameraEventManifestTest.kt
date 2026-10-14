package com.acme.transport.safety.cameraevent.rollup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CameraEventManifestTest {
    @Test
    fun `starts in idle`() {
        val item = newCameraEventManifest("code-700", "name-643")
        assertEquals(CameraEventManifestStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to ready`() {
        val item = newCameraEventManifest("code-700", "name-643")
        try {
            item.moveTo(CameraEventManifestStatus.READY)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CameraEventManifestStatus.READY, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newCameraEventManifest("code-700", "name-643")
        item.status = CameraEventManifestStatus.STOPPED
        assertFails("move") { item.moveTo(CameraEventManifestStatus.IDLE) }
    }
}

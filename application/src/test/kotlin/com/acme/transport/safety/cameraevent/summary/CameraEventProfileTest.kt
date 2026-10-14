package com.acme.transport.safety.cameraevent.summary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CameraEventProfileTest {
    @Test
    fun `starts in idle`() {
        val item = newCameraEventProfile("code-551", "title-336")
        assertEquals(CameraEventProfileStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to warming`() {
        val item = newCameraEventProfile("code-551", "title-336")
        try {
            item.moveTo(CameraEventProfileStatus.WARMING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CameraEventProfileStatus.WARMING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newCameraEventProfile("code-551", "title-336")
        item.status = CameraEventProfileStatus.STOPPED
        assertFails("move") { item.moveTo(CameraEventProfileStatus.IDLE) }
    }
}

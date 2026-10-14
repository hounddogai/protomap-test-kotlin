package com.acme.transport.safety.cameraevent.group

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CameraEventOverrideTest {
    @Test
    fun `starts in new`() {
        val item = newCameraEventOverride("code-554", "name-758")
        assertEquals(CameraEventOverrideStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to queued`() {
        val item = newCameraEventOverride("code-554", "name-758")
        try {
            item.moveTo(CameraEventOverrideStatus.QUEUED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CameraEventOverrideStatus.QUEUED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newCameraEventOverride("code-554", "name-758")
        item.status = CameraEventOverrideStatus.FAILED
        assertFails("move") { item.moveTo(CameraEventOverrideStatus.NEW) }
    }
}

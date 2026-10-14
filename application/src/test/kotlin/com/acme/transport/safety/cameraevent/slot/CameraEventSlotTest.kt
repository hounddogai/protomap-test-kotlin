package com.acme.transport.safety.cameraevent.slot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CameraEventSlotTest {
    @Test
    fun `starts in created`() {
        val item = newCameraEventSlot("code-236", "title-124")
        assertEquals(CameraEventSlotStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newCameraEventSlot("code-236", "title-124")
        try {
            item.moveTo(CameraEventSlotStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CameraEventSlotStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newCameraEventSlot("code-236", "title-124")
        item.status = CameraEventSlotStatus.RETIRED
        assertFails("move") { item.moveTo(CameraEventSlotStatus.CREATED) }
    }
}

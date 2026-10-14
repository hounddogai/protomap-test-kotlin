package com.acme.transport.safety.cameraevent.eventslot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ManualCameraEventSlotTest {
    @Test
    fun `starts in proposed`() {
        val item = newManualCameraEventSlot("code-414", "label-252")
        assertEquals(ManualCameraEventSlotStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to rejected`() {
        val item = newManualCameraEventSlot("code-414", "label-252")
        try {
            item.moveTo(ManualCameraEventSlotStatus.REJECTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ManualCameraEventSlotStatus.REJECTED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newManualCameraEventSlot("code-414", "label-252")
        item.status = ManualCameraEventSlotStatus.WITHDRAWN
        assertFails("move") { item.moveTo(ManualCameraEventSlotStatus.PROPOSED) }
    }
}

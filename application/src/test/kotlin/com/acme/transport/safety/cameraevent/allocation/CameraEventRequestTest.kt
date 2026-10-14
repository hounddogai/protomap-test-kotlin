package com.acme.transport.safety.cameraevent.allocation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CameraEventRequestTest {
    @Test
    fun `starts in created`() {
        val item = newCameraEventRequest("code-760", "label-540")
        assertEquals(CameraEventRequestStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newCameraEventRequest("code-760", "label-540")
        try {
            item.moveTo(CameraEventRequestStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CameraEventRequestStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newCameraEventRequest("code-760", "label-540")
        item.status = CameraEventRequestStatus.RETIRED
        assertFails("move") { item.moveTo(CameraEventRequestStatus.CREATED) }
    }
}

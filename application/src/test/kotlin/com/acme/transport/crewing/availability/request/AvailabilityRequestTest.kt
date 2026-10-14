package com.acme.transport.crewing.availability.request

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AvailabilityRequestTest {
    @Test
    fun `starts in idle`() {
        val item = newAvailabilityRequest("code-524", "label-185")
        assertEquals(AvailabilityRequestStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to ready`() {
        val item = newAvailabilityRequest("code-524", "label-185")
        try {
            item.moveTo(AvailabilityRequestStatus.READY)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AvailabilityRequestStatus.READY, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newAvailabilityRequest("code-524", "label-185")
        item.status = AvailabilityRequestStatus.STOPPED
        assertFails("move") { item.moveTo(AvailabilityRequestStatus.IDLE) }
    }
}

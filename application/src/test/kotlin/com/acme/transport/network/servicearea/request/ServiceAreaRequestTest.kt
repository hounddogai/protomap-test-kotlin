package com.acme.transport.network.servicearea.request

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ServiceAreaRequestTest {
    @Test
    fun `starts in idle`() {
        val item = newServiceAreaRequest("code-532", "label-852")
        assertEquals(ServiceAreaRequestStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to ready`() {
        val item = newServiceAreaRequest("code-532", "label-852")
        try {
            item.moveTo(ServiceAreaRequestStatus.READY)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ServiceAreaRequestStatus.READY, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newServiceAreaRequest("code-532", "label-852")
        item.status = ServiceAreaRequestStatus.STOPPED
        assertFails("move") { item.moveTo(ServiceAreaRequestStatus.IDLE) }
    }
}

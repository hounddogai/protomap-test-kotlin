package com.acme.transport.lastmile.redelivery.request

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RedeliveryRequestTest {
    @Test
    fun `starts in idle`() {
        val item = newRedeliveryRequest("code-670", "name-256")
        assertEquals(RedeliveryRequestStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to warming`() {
        val item = newRedeliveryRequest("code-670", "name-256")
        try {
            item.moveTo(RedeliveryRequestStatus.WARMING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RedeliveryRequestStatus.WARMING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newRedeliveryRequest("code-670", "name-256")
        item.status = RedeliveryRequestStatus.STOPPED
        assertFails("move") { item.moveTo(RedeliveryRequestStatus.IDLE) }
    }
}

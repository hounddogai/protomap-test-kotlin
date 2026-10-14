package com.acme.transport.freightpay.dispute.window

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DisputeRequestTest {
    @Test
    fun `starts in requested`() {
        val item = newDisputeRequest("code-396", "name-915")
        assertEquals(DisputeRequestStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newDisputeRequest("code-396", "name-915")
        try {
            item.moveTo(DisputeRequestStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DisputeRequestStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newDisputeRequest("code-396", "name-915")
        item.status = DisputeRequestStatus.EXPIRED
        assertFails("move") { item.moveTo(DisputeRequestStatus.REQUESTED) }
    }
}

package com.acme.transport.linehaul.capacity.capacityrequest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LenientCapacityRequestTest {
    @Test
    fun `starts in requested`() {
        val item = newLenientCapacityRequest("code-87", "title-401")
        assertEquals(LenientCapacityRequestStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newLenientCapacityRequest("code-87", "title-401")
        try {
            item.moveTo(LenientCapacityRequestStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LenientCapacityRequestStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newLenientCapacityRequest("code-87", "title-401")
        item.status = LenientCapacityRequestStatus.EXPIRED
        assertFails("move") { item.moveTo(LenientCapacityRequestStatus.REQUESTED) }
    }
}

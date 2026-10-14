package com.acme.transport.linehaul.capacity.request

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CapacityRequestTest {
    @Test
    fun `starts in proposed`() {
        val item = newCapacityRequest("code-428", "title-86")
        assertEquals(CapacityRequestStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newCapacityRequest("code-428", "title-86")
        try {
            item.moveTo(CapacityRequestStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CapacityRequestStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newCapacityRequest("code-428", "title-86")
        item.status = CapacityRequestStatus.WITHDRAWN
        assertFails("move") { item.moveTo(CapacityRequestStatus.PROPOSED) }
    }
}

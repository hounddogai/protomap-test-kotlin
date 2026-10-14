package com.acme.transport.safety.violation.segment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ViolationRequestTest {
    @Test
    fun `starts in draft`() {
        val item = newViolationRequest("code-509", "name-302")
        assertEquals(ViolationRequestStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to pending`() {
        val item = newViolationRequest("code-509", "name-302")
        try {
            item.moveTo(ViolationRequestStatus.PENDING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ViolationRequestStatus.PENDING, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newViolationRequest("code-509", "name-302")
        item.status = ViolationRequestStatus.CLOSED
        assertFails("move") { item.moveTo(ViolationRequestStatus.DRAFT) }
    }
}

package com.acme.transport.safety.coaching.rollup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CoachingRequestTest {
    @Test
    fun `starts in requested`() {
        val item = newCoachingRequest("code-643", "title-208")
        assertEquals(CoachingRequestStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to revoked`() {
        val item = newCoachingRequest("code-643", "title-208")
        try {
            item.moveTo(CoachingRequestStatus.REVOKED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CoachingRequestStatus.REVOKED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newCoachingRequest("code-643", "title-208")
        item.status = CoachingRequestStatus.EXPIRED
        assertFails("move") { item.moveTo(CoachingRequestStatus.REQUESTED) }
    }
}

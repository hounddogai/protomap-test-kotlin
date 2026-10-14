package com.acme.transport.fleetops.tire.request

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PrimaryTireRequestTest {
    @Test
    fun `starts in proposed`() {
        val item = newPrimaryTireRequest("code-657", "title-171")
        assertEquals(PrimaryTireRequestStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to rejected`() {
        val item = newPrimaryTireRequest("code-657", "title-171")
        try {
            item.moveTo(PrimaryTireRequestStatus.REJECTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PrimaryTireRequestStatus.REJECTED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newPrimaryTireRequest("code-657", "title-171")
        item.status = PrimaryTireRequestStatus.WITHDRAWN
        assertFails("move") { item.moveTo(PrimaryTireRequestStatus.PROPOSED) }
    }
}

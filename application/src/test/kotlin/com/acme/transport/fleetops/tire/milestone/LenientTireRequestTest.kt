package com.acme.transport.fleetops.tire.milestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LenientTireRequestTest {
    @Test
    fun `starts in draft`() {
        val item = newLenientTireRequest("code-164", "title-940")
        assertEquals(LenientTireRequestStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to pending`() {
        val item = newLenientTireRequest("code-164", "title-940")
        try {
            item.moveTo(LenientTireRequestStatus.PENDING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LenientTireRequestStatus.PENDING, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newLenientTireRequest("code-164", "title-940")
        item.status = LenientTireRequestStatus.CLOSED
        assertFails("move") { item.moveTo(LenientTireRequestStatus.DRAFT) }
    }
}

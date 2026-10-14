package com.acme.transport.linehaul.trailer.request

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TrailerRequestTest {
    @Test
    fun `starts in requested`() {
        val item = newTrailerRequest("code-13", "title-960")
        assertEquals(TrailerRequestStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to revoked`() {
        val item = newTrailerRequest("code-13", "title-960")
        try {
            item.moveTo(TrailerRequestStatus.REVOKED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TrailerRequestStatus.REVOKED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newTrailerRequest("code-13", "title-960")
        item.status = TrailerRequestStatus.EXPIRED
        assertFails("move") { item.moveTo(TrailerRequestStatus.REQUESTED) }
    }
}

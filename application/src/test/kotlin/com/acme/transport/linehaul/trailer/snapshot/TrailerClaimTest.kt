package com.acme.transport.linehaul.trailer.snapshot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TrailerClaimTest {
    @Test
    fun `starts in requested`() {
        val item = newTrailerClaim("code-822", "label-506")
        assertEquals(TrailerClaimStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newTrailerClaim("code-822", "label-506")
        try {
            item.moveTo(TrailerClaimStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TrailerClaimStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newTrailerClaim("code-822", "label-506")
        item.status = TrailerClaimStatus.EXPIRED
        assertFails("move") { item.moveTo(TrailerClaimStatus.REQUESTED) }
    }
}

package com.acme.transport.terminal.traileryard.tier

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TrailerYardClaimTest {
    @Test
    fun `starts in requested`() {
        val item = newTrailerYardClaim("code-748", "name-33")
        assertEquals(TrailerYardClaimStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to expired`() {
        val item = newTrailerYardClaim("code-748", "name-33")
        try {
            item.moveTo(TrailerYardClaimStatus.EXPIRED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TrailerYardClaimStatus.EXPIRED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newTrailerYardClaim("code-748", "name-33")
        item.status = TrailerYardClaimStatus.EXPIRED
        assertFails("move") { item.moveTo(TrailerYardClaimStatus.REQUESTED) }
    }
}

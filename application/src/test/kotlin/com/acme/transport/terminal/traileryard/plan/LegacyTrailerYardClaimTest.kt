package com.acme.transport.terminal.traileryard.plan

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LegacyTrailerYardClaimTest {
    @Test
    fun `starts in open`() {
        val item = newLegacyTrailerYardClaim("code-961", "name-257")
        assertEquals(LegacyTrailerYardClaimStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in review`() {
        val item = newLegacyTrailerYardClaim("code-961", "name-257")
        try {
            item.moveTo(LegacyTrailerYardClaimStatus.IN_REVIEW)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LegacyTrailerYardClaimStatus.IN_REVIEW, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newLegacyTrailerYardClaim("code-961", "name-257")
        item.status = LegacyTrailerYardClaimStatus.ARCHIVED
        assertFails("move") { item.moveTo(LegacyTrailerYardClaimStatus.OPEN) }
    }
}

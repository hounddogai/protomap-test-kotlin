package com.acme.transport.terminal.traileryard.claim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ScheduledTrailerYardClaimTest {
    @Test
    fun `starts in created`() {
        val item = newScheduledTrailerYardClaim("code-762", "name-285")
        assertEquals(ScheduledTrailerYardClaimStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newScheduledTrailerYardClaim("code-762", "name-285")
        try {
            item.moveTo(ScheduledTrailerYardClaimStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ScheduledTrailerYardClaimStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newScheduledTrailerYardClaim("code-762", "name-285")
        item.status = ScheduledTrailerYardClaimStatus.RETIRED
        assertFails("move") { item.moveTo(ScheduledTrailerYardClaimStatus.CREATED) }
    }
}

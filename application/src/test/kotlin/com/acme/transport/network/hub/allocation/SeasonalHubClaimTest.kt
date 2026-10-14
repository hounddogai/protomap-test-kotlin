package com.acme.transport.network.hub.allocation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class SeasonalHubClaimTest {
    @Test
    fun `starts in idle`() {
        val item = newSeasonalHubClaim("code-507", "title-385")
        assertEquals(SeasonalHubClaimStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to ready`() {
        val item = newSeasonalHubClaim("code-507", "title-385")
        try {
            item.moveTo(SeasonalHubClaimStatus.READY)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(SeasonalHubClaimStatus.READY, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newSeasonalHubClaim("code-507", "title-385")
        item.status = SeasonalHubClaimStatus.STOPPED
        assertFails("move") { item.moveTo(SeasonalHubClaimStatus.IDLE) }
    }
}

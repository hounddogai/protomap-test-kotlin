package com.acme.transport.emissions.fuelburn.claim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AdaptiveFuelBurnClaimTest {
    @Test
    fun `starts in idle`() {
        val item = newAdaptiveFuelBurnClaim("code-644", "label-630")
        assertEquals(AdaptiveFuelBurnClaimStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to stopped`() {
        val item = newAdaptiveFuelBurnClaim("code-644", "label-630")
        try {
            item.moveTo(AdaptiveFuelBurnClaimStatus.STOPPED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AdaptiveFuelBurnClaimStatus.STOPPED, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newAdaptiveFuelBurnClaim("code-644", "label-630")
        item.status = AdaptiveFuelBurnClaimStatus.STOPPED
        assertFails("move") { item.moveTo(AdaptiveFuelBurnClaimStatus.IDLE) }
    }
}

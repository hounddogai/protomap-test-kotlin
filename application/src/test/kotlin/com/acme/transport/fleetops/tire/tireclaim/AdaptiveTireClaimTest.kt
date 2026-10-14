package com.acme.transport.fleetops.tire.tireclaim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AdaptiveTireClaimTest {
    @Test
    fun `starts in created`() {
        val item = newAdaptiveTireClaim("code-848", "title-794")
        assertEquals(AdaptiveTireClaimStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newAdaptiveTireClaim("code-848", "title-794")
        try {
            item.moveTo(AdaptiveTireClaimStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AdaptiveTireClaimStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newAdaptiveTireClaim("code-848", "title-794")
        item.status = AdaptiveTireClaimStatus.RETIRED
        assertFails("move") { item.moveTo(AdaptiveTireClaimStatus.CREATED) }
    }
}

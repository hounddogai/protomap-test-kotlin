package com.acme.transport.safety.nearmiss.missclaim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AdaptiveNearMissClaimTest {
    @Test
    fun `starts in draft`() {
        val item = newAdaptiveNearMissClaim("code-459", "name-427")
        assertEquals(AdaptiveNearMissClaimStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to pending`() {
        val item = newAdaptiveNearMissClaim("code-459", "name-427")
        try {
            item.moveTo(AdaptiveNearMissClaimStatus.PENDING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AdaptiveNearMissClaimStatus.PENDING, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newAdaptiveNearMissClaim("code-459", "name-427")
        item.status = AdaptiveNearMissClaimStatus.CLOSED
        assertFails("move") { item.moveTo(AdaptiveNearMissClaimStatus.DRAFT) }
    }
}

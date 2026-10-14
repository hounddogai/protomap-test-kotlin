package com.acme.transport.fleetops.tire.claim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TireClaimTest {
    @Test
    fun `starts in proposed`() {
        val item = newTireClaim("code-992", "label-798")
        assertEquals(TireClaimStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newTireClaim("code-992", "label-798")
        try {
            item.moveTo(TireClaimStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TireClaimStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newTireClaim("code-992", "label-798")
        item.status = TireClaimStatus.WITHDRAWN
        assertFails("move") { item.moveTo(TireClaimStatus.PROPOSED) }
    }
}

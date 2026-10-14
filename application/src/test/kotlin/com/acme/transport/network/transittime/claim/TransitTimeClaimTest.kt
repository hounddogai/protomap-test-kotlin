package com.acme.transport.network.transittime.claim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TransitTimeClaimTest {
    @Test
    fun `starts in draft`() {
        val item = newTransitTimeClaim("code-391", "name-958")
        assertEquals(TransitTimeClaimStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to closed`() {
        val item = newTransitTimeClaim("code-391", "name-958")
        try {
            item.moveTo(TransitTimeClaimStatus.CLOSED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TransitTimeClaimStatus.CLOSED, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newTransitTimeClaim("code-391", "name-958")
        item.status = TransitTimeClaimStatus.CLOSED
        assertFails("move") { item.moveTo(TransitTimeClaimStatus.DRAFT) }
    }
}

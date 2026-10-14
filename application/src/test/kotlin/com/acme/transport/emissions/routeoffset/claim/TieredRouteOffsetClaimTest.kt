package com.acme.transport.emissions.routeoffset.claim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TieredRouteOffsetClaimTest {
    @Test
    fun `starts in open`() {
        val item = newTieredRouteOffsetClaim("code-764", "title-679")
        assertEquals(TieredRouteOffsetClaimStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in review`() {
        val item = newTieredRouteOffsetClaim("code-764", "title-679")
        try {
            item.moveTo(TieredRouteOffsetClaimStatus.IN_REVIEW)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TieredRouteOffsetClaimStatus.IN_REVIEW, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newTieredRouteOffsetClaim("code-764", "title-679")
        item.status = TieredRouteOffsetClaimStatus.ARCHIVED
        assertFails("move") { item.moveTo(TieredRouteOffsetClaimStatus.OPEN) }
    }
}

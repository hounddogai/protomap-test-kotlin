package com.acme.transport.terminal.crossdock.regionaldockclaim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LenientCrossDockClaimTest {
    @Test
    fun `starts in draft`() {
        val item = newLenientCrossDockClaim("code-374", "name-528")
        assertEquals(LenientCrossDockClaimStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to active`() {
        val item = newLenientCrossDockClaim("code-374", "name-528")
        try {
            item.moveTo(LenientCrossDockClaimStatus.ACTIVE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LenientCrossDockClaimStatus.ACTIVE, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newLenientCrossDockClaim("code-374", "name-528")
        item.status = LenientCrossDockClaimStatus.CLOSED
        assertFails("move") { item.moveTo(LenientCrossDockClaimStatus.DRAFT) }
    }
}

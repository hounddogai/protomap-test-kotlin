package com.acme.transport.linehaul.relay.adjustment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RelayClaimTest {
    @Test
    fun `starts in draft`() {
        val item = newRelayClaim("code-383", "title-211")
        assertEquals(RelayClaimStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to active`() {
        val item = newRelayClaim("code-383", "title-211")
        try {
            item.moveTo(RelayClaimStatus.ACTIVE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RelayClaimStatus.ACTIVE, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newRelayClaim("code-383", "title-211")
        item.status = RelayClaimStatus.CLOSED
        assertFails("move") { item.moveTo(RelayClaimStatus.DRAFT) }
    }
}

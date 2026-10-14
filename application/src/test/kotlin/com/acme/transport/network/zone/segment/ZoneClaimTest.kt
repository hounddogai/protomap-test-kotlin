package com.acme.transport.network.zone.segment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ZoneClaimTest {
    @Test
    fun `starts in draft`() {
        val item = newZoneClaim("code-334", "label-896")
        assertEquals(ZoneClaimStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to closed`() {
        val item = newZoneClaim("code-334", "label-896")
        try {
            item.moveTo(ZoneClaimStatus.CLOSED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ZoneClaimStatus.CLOSED, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newZoneClaim("code-334", "label-896")
        item.status = ZoneClaimStatus.CLOSED
        assertFails("move") { item.moveTo(ZoneClaimStatus.DRAFT) }
    }
}

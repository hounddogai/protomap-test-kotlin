package com.acme.transport.network.transittime.allocation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TransitTimeAllocationTest {
    @Test
    fun `starts in draft`() {
        val item = newTransitTimeAllocation("code-164", "name-245")
        assertEquals(TransitTimeAllocationStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to active`() {
        val item = newTransitTimeAllocation("code-164", "name-245")
        try {
            item.moveTo(TransitTimeAllocationStatus.ACTIVE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TransitTimeAllocationStatus.ACTIVE, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newTransitTimeAllocation("code-164", "name-245")
        item.status = TransitTimeAllocationStatus.CLOSED
        assertFails("move") { item.moveTo(TransitTimeAllocationStatus.DRAFT) }
    }
}

package com.acme.transport.fleetops.leasereturn.adaptivereturnledger

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DefaultLeaseReturnEntryTest {
    @Test
    fun `starts in draft`() {
        val item = newDefaultLeaseReturnEntry("code-349", "name-312")
        assertEquals(DefaultLeaseReturnEntryStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to pending`() {
        val item = newDefaultLeaseReturnEntry("code-349", "name-312")
        try {
            item.moveTo(DefaultLeaseReturnEntryStatus.PENDING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DefaultLeaseReturnEntryStatus.PENDING, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newDefaultLeaseReturnEntry("code-349", "name-312")
        item.status = DefaultLeaseReturnEntryStatus.CLOSED
        assertFails("move") { item.moveTo(DefaultLeaseReturnEntryStatus.DRAFT) }
    }
}

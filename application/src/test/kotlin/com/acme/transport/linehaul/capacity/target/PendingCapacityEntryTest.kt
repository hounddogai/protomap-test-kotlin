package com.acme.transport.linehaul.capacity.target

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PendingCapacityEntryTest {
    @Test
    fun `starts in requested`() {
        val item = newPendingCapacityEntry("code-998", "label-57")
        assertEquals(PendingCapacityEntryStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to revoked`() {
        val item = newPendingCapacityEntry("code-998", "label-57")
        try {
            item.moveTo(PendingCapacityEntryStatus.REVOKED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PendingCapacityEntryStatus.REVOKED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newPendingCapacityEntry("code-998", "label-57")
        item.status = PendingCapacityEntryStatus.EXPIRED
        assertFails("move") { item.moveTo(PendingCapacityEntryStatus.REQUESTED) }
    }
}

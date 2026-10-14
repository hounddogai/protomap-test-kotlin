package com.acme.transport.fleetops.leasereturn.returnentry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LegacyLeaseReturnEntryTest {
    @Test
    fun `starts in idle`() {
        val item = newLegacyLeaseReturnEntry("code-605", "name-921")
        assertEquals(LegacyLeaseReturnEntryStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to ready`() {
        val item = newLegacyLeaseReturnEntry("code-605", "name-921")
        try {
            item.moveTo(LegacyLeaseReturnEntryStatus.READY)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LegacyLeaseReturnEntryStatus.READY, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newLegacyLeaseReturnEntry("code-605", "name-921")
        item.status = LegacyLeaseReturnEntryStatus.STOPPED
        assertFails("move") { item.moveTo(LegacyLeaseReturnEntryStatus.IDLE) }
    }
}

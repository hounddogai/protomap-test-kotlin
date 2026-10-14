package com.acme.transport.network.zone.digest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ZoneAllocationTest {
    @Test
    fun `starts in planned`() {
        val item = newZoneAllocation("code-733", "title-761")
        assertEquals(ZoneAllocationStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to scheduled`() {
        val item = newZoneAllocation("code-733", "title-761")
        try {
            item.moveTo(ZoneAllocationStatus.SCHEDULED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ZoneAllocationStatus.SCHEDULED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newZoneAllocation("code-733", "title-761")
        item.status = ZoneAllocationStatus.CANCELLED
        assertFails("move") { item.moveTo(ZoneAllocationStatus.PLANNED) }
    }
}

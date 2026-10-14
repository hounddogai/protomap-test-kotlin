package com.acme.transport.fleetops.leasereturn.returnrequest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LegacyLeaseReturnRequestTest {
    @Test
    fun `starts in idle`() {
        val item = newLegacyLeaseReturnRequest("code-775", "title-417")
        assertEquals(LegacyLeaseReturnRequestStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to draining`() {
        val item = newLegacyLeaseReturnRequest("code-775", "title-417")
        try {
            item.moveTo(LegacyLeaseReturnRequestStatus.DRAINING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LegacyLeaseReturnRequestStatus.DRAINING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newLegacyLeaseReturnRequest("code-775", "title-417")
        item.status = LegacyLeaseReturnRequestStatus.STOPPED
        assertFails("move") { item.moveTo(LegacyLeaseReturnRequestStatus.IDLE) }
    }
}

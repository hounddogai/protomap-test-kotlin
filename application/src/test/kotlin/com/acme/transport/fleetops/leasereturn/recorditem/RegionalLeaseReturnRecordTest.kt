package com.acme.transport.fleetops.leasereturn.recorditem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RegionalLeaseReturnRecordTest {
    @Test
    fun `starts in requested`() {
        val item = newRegionalLeaseReturnRecord("code-649", "name-852")
        assertEquals(RegionalLeaseReturnRecordStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newRegionalLeaseReturnRecord("code-649", "name-852")
        try {
            item.moveTo(RegionalLeaseReturnRecordStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RegionalLeaseReturnRecordStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newRegionalLeaseReturnRecord("code-649", "name-852")
        item.status = RegionalLeaseReturnRecordStatus.EXPIRED
        assertFails("move") { item.moveTo(RegionalLeaseReturnRecordStatus.REQUESTED) }
    }
}

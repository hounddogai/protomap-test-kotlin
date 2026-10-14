package com.acme.transport.fleetops.leasereturn.draft

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RegionalLeaseReturnPolicyTest {
    @Test
    fun `starts in new`() {
        val item = newRegionalLeaseReturnPolicy("code-61", "label-510")
        assertEquals(RegionalLeaseReturnPolicyStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to queued`() {
        val item = newRegionalLeaseReturnPolicy("code-61", "label-510")
        try {
            item.moveTo(RegionalLeaseReturnPolicyStatus.QUEUED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RegionalLeaseReturnPolicyStatus.QUEUED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newRegionalLeaseReturnPolicy("code-61", "label-510")
        item.status = RegionalLeaseReturnPolicyStatus.FAILED
        assertFails("move") { item.moveTo(RegionalLeaseReturnPolicyStatus.NEW) }
    }
}

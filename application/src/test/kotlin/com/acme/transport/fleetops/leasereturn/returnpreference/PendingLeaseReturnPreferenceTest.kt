package com.acme.transport.fleetops.leasereturn.returnpreference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PendingLeaseReturnPreferenceTest {
    @Test
    fun `starts in planned`() {
        val item = newPendingLeaseReturnPreference("code-402", "title-460")
        assertEquals(PendingLeaseReturnPreferenceStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to completed`() {
        val item = newPendingLeaseReturnPreference("code-402", "title-460")
        try {
            item.moveTo(PendingLeaseReturnPreferenceStatus.COMPLETED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PendingLeaseReturnPreferenceStatus.COMPLETED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newPendingLeaseReturnPreference("code-402", "title-460")
        item.status = PendingLeaseReturnPreferenceStatus.CANCELLED
        assertFails("move") { item.moveTo(PendingLeaseReturnPreferenceStatus.PLANNED) }
    }
}

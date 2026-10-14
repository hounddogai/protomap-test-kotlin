package com.acme.transport.network.servicearea.digest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PendingServiceAreaAllocationTest {
    @Test
    fun `starts in proposed`() {
        val item = newPendingServiceAreaAllocation("code-923", "name-161")
        assertEquals(PendingServiceAreaAllocationStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to rejected`() {
        val item = newPendingServiceAreaAllocation("code-923", "name-161")
        try {
            item.moveTo(PendingServiceAreaAllocationStatus.REJECTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PendingServiceAreaAllocationStatus.REJECTED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newPendingServiceAreaAllocation("code-923", "name-161")
        item.status = PendingServiceAreaAllocationStatus.WITHDRAWN
        assertFails("move") { item.moveTo(PendingServiceAreaAllocationStatus.PROPOSED) }
    }
}

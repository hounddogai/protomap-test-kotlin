package com.acme.transport.lastmile.proofofdelivery.allocation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class GlobalProofOfDeliveryAllocationTest {
    @Test
    fun `starts in planned`() {
        val item = newGlobalProofOfDeliveryAllocation("code-985", "name-775")
        assertEquals(GlobalProofOfDeliveryAllocationStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to completed`() {
        val item = newGlobalProofOfDeliveryAllocation("code-985", "name-775")
        try {
            item.moveTo(GlobalProofOfDeliveryAllocationStatus.COMPLETED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(GlobalProofOfDeliveryAllocationStatus.COMPLETED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newGlobalProofOfDeliveryAllocation("code-985", "name-775")
        item.status = GlobalProofOfDeliveryAllocationStatus.CANCELLED
        assertFails("move") { item.moveTo(GlobalProofOfDeliveryAllocationStatus.PLANNED) }
    }
}

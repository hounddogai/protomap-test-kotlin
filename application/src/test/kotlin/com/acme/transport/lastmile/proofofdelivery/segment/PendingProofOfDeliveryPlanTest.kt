package com.acme.transport.lastmile.proofofdelivery.segment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PendingProofOfDeliveryPlanTest {
    @Test
    fun `starts in open`() {
        val item = newPendingProofOfDeliveryPlan("code-608", "title-993")
        assertEquals(PendingProofOfDeliveryPlanStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to reopened`() {
        val item = newPendingProofOfDeliveryPlan("code-608", "title-993")
        try {
            item.moveTo(PendingProofOfDeliveryPlanStatus.REOPENED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PendingProofOfDeliveryPlanStatus.REOPENED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newPendingProofOfDeliveryPlan("code-608", "title-993")
        item.status = PendingProofOfDeliveryPlanStatus.ARCHIVED
        assertFails("move") { item.moveTo(PendingProofOfDeliveryPlanStatus.OPEN) }
    }
}
